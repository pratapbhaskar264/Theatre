package com.bhaskar.theatre.service;



import com.bhaskar.theatre.dto.BookingEvent;
import com.bhaskar.theatre.dto.ReservationRequestDto;
import com.bhaskar.theatre.entity.Reservation;
import com.bhaskar.theatre.entity.Seat;
import com.bhaskar.theatre.entity.User;
import com.bhaskar.theatre.enums.ReservationStatus;
import com.bhaskar.theatre.enums.SeatStatus;
import com.bhaskar.theatre.exception.*;
import com.bhaskar.theatre.repository.ReservationRepository;
import com.bhaskar.theatre.repository.SeatRepository;
import com.bhaskar.theatre.repository.ShowRepository;
import com.bhaskar.theatre.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.redisson.api.RLock;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static com.bhaskar.theatre.constant.ExceptionMessages.*;

@Service
public class ReservationService {

    private final SeatLockManager seatLockManager;
    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final ShowRepository showRepository;
    private final UserRepository userRepository;
    private final RedisService redisService;
    private final RedissonClient redissonClient;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired
    public ReservationService(SeatLockManager seatLockManager,
                              ReservationRepository reservationRepository,
                              SeatRepository seatRepository,
                              ShowRepository showRepository,
                              UserRepository userRepository, RedisService redisService, RedissonClient redissonClient) {
        this.seatLockManager = seatLockManager;
        this.reservationRepository = reservationRepository;
        this.seatRepository = seatRepository;
        this.showRepository = showRepository;
        this.userRepository = userRepository;
        //Annotations like @RestController, @Service, or @Component tell Spring:
        // "Manage this class for me." By default, Spring's management style is Singleton Scope.
        // Whether you use constructor injection, field injection
        // (@Autowired on the variable), or setter injection, the
        // class remains a singleton because of that top-level annotation.
        this.redisService = redisService;
        this.redissonClient = redissonClient;
    }

    @Transactional
    public Reservation createReservation(ReservationRequestDto reservationRequestDto, String currentUserName) {
        return showRepository.findById(reservationRequestDto.getShowId()).map(show -> {

            // 1. Fetch User and Seats
            User user = userRepository.findByUsername(currentUserName)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Seat> seats = seatRepository.findByIdInAndShowId(
                    reservationRequestDto.getSeatIdsReserve(), show.getId());

            if (seats.size() != reservationRequestDto.getSeatIdsReserve().size()) {
                throw new RuntimeException("One or more seats not found.");
            }

            List<RLock> acquiredLocks = new ArrayList<>();
            try {
                // 2. Distributed Locking (Sorted to prevent Deadlocks)
                List<Long> sortedIds = reservationRequestDto.getSeatIdsReserve().stream().sorted().toList();
                for (Long seatId : sortedIds) {
                    RLock seatLock = redissonClient.getLock("lock:seat:" + seatId);
                    if (seatLock.tryLock(15, 10, TimeUnit.SECONDS)) {
                        acquiredLocks.add(seatLock);
                    } else {
                        throw new SeatLockAccquiredException(SEAT_LOCK_ACCQUIRED, HttpStatus.CONFLICT);
                    }
                }

                // 3. Validation
                if (seats.stream().anyMatch(s -> s.getStatus().equals(SeatStatus.BOOKED))) {
                    throw new SeatAlreadyBookedException(SEAT_ALREADY_BOOKED, HttpStatus.BAD_REQUEST);
                }

                double amountToBePaid = seats.stream().mapToDouble(Seat::getPrice).sum();
                if (Double.compare(reservationRequestDto.getAmount(), amountToBePaid) != 0) {
                    throw new AmountNotMatchException(AMOUNT_NOT_MATCH, HttpStatus.BAD_REQUEST, amountToBePaid);
                }

                // 4. Update Database
                seats.forEach(seat -> seat.setStatus(SeatStatus.BOOKED));
                seatRepository.saveAll(seats);

                Reservation savedReservation = reservationRepository.save(Reservation.builder()
                        .reservationStatus(ReservationStatus.BOOKED)
                        .seatsReserved(seats)
                        .show(show)
                        .user(user)
                        .amountPaid(reservationRequestDto.getAmount())
                        .createdAt(LocalDateTime.now())
                        .build());

                // 5. Cache Eviction (Still inside transaction)
                redisService.delete("seats:show:" + show.getId());

                // 6. Kafka "Fire after Commit" Logic
                // Inside your createReservation method, after the save and inside afterCommit:
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        // ULTRA-SAFE MAPPING: Use raw data to break the loop
                        BookingEvent event = BookingEvent.builder()
                                .reservationId(savedReservation.getId())
                                .username(currentUserName) // Pass string directly
                                .showId(reservationRequestDto.getShowId()) // Use ID from request
                                .movieName(show.getMovie().getMovieName()) // Access movieName directly
                                .seatIds(reservationRequestDto.getSeatIdsReserve()) // Use IDs from request
                                .amount(reservationRequestDto.getAmount())
                                .status("BOOKED")
                                .build();

                        // Log to console so you can see progress before the send
                        System.out.println("KAFKA: Preparing to send message for Reservation ID: " + event.getReservationId());

                        // Send the DTO, not the Entity
                        kafkaTemplate.send("theatre-activity", String.valueOf(event.getReservationId()), event);

                        System.out.println("KAFKA: Message sent successfully to topic 'theatre-activity'");
                    }
                });
                return savedReservation;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted", e);
            } finally {
                acquiredLocks.forEach(lock -> {
                    if (lock.isHeldByCurrentThread()) lock.unlock();
                });
            }
        }).orElseThrow(() -> new ShowNotFoundException(SHOW_NOT_FOUND, HttpStatus.BAD_REQUEST));
    }
    public Reservation getReservationById(String currentUserName, long reservationId) {
        // This query fetches the reservation row and ONLY its linked seats
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElse(null);

        // Security check we discussed
        if (!reservation.getUser().getUsername().equals(currentUserName)) {
            throw new UnAuthorizedException(UNAUTHORIZED_EXCEPTION , HttpStatus.BAD_REQUEST);
        }

        return reservation;
    }

    @Transactional
    public Reservation cancelReservation(long reservationId) {
        return reservationRepository.findById(reservationId)
                .map(reservation -> {
                    // 1. Time Check
                    if (LocalDateTime.now().isAfter(reservation.getShow().getStartTime()))
                        throw new ShowStartedException(SHOW_STARTED_EXCEPTION, HttpStatus.BAD_REQUEST);

                    // 2. Free the Seats
                    reservation.getSeatsReserved().forEach(seat -> seat.setStatus(SeatStatus.UNBOOKED));
                    seatRepository.saveAll(reservation.getSeatsReserved());

                    // 3. Update Status
                    reservation.setReservationStatus(ReservationStatus.CANCELED);

                    // 4. EVICT CACHE for the show this reservation belonged to
                    String key = "seats:show:" + reservation.getShow().getId();
                    redisService.delete(key);

                    return reservationRepository.save(reservation);
                })
                .orElseThrow(() -> new ReservationNotFoundException(RESERVATION_NOT_FOUND, HttpStatus.NOT_FOUND));
    }
    public Page<Reservation> getReservationsByUsername(String username, int page, int size) {
        return reservationRepository.findByUserUsername(username, PageRequest.of(page, size));
    }

    public Page<Reservation> filterReservations(Long theaterId, Long movieId, Long userId, String status, String date, int page, int size) {
        // Convert String status to Enum if necessary
        ReservationStatus resStatus = ReservationStatus.valueOf(status);
        return reservationRepository.filterReservations(theaterId, movieId, userId, resStatus, PageRequest.of(page, size));
    }

}
