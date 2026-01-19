package com.bhaskar.theatre.service;



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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.redisson.api.RLock;

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

            // Fetch the seats requested
            List<Seat> seats = reservationRequestDto.getSeatIdsReserve().stream()
                    .map(seatRepository::findById)
                    .map(Optional::get)
                    .toList();

            // Track acquired Redisson locks (if possible line 91)

            List<RLock> acquiredLocks = new ArrayList<>();

            try {
                // 1. Sort IDs to prevent Deadlock
                List<Long> sortedIds = reservationRequestDto.getSeatIdsReserve().stream().sorted().toList();

                // 2. Acquire Distributed Locks
                for (Long seatId : sortedIds) {
                    // Get a lock specific to this seat ID from Redis
                    RLock seatLock = redissonClient.getLock("lock:seat:" + seatId);

                    // tryLock(waitTime, leaseTime, unit)
                    // We wait 5s for others to finish; Auto-release after 10s if server crashes
                    // actual redis client implementation
                    if (seatLock.tryLock(5, 10, TimeUnit.SECONDS)) {
                        acquiredLocks.add(seatLock);
                    } else {
                        // Using your existing exception
                        throw new SeatLockAccquiredException(SEAT_LOCK_ACCQUIRED, HttpStatus.CONFLICT);
                    }
                }

                // 3. Logic checks (Status check)
                boolean anyBookedSeat = seats.stream()
                        .anyMatch(s -> s.getStatus().equals(SeatStatus.BOOKED));

                if (anyBookedSeat) {
                    throw new SeatAlreadyBookedException(SEAT_ALREADY_BOOKED, HttpStatus.BAD_REQUEST);
                }

                // 4. Amount validation
                Double amountToBePaid = seats.stream().map(Seat::getPrice).reduce(0.0, Double::sum);
                if (!reservationRequestDto.getAmount().equals(amountToBePaid)) {
                    throw new AmountNotMatchException(AMOUNT_NOT_MATCH, HttpStatus.BAD_REQUEST, amountToBePaid);
                }

                // 5. Update Database
                seats.forEach(seat -> {
                    seat.setStatus(SeatStatus.BOOKED);
                    seatRepository.save(seat);
                });

                // 6. Redis Cache Eviction for seat structure
                String cacheKey = "seats:show:" + reservationRequestDto.getShowId();
                redisService.delete(cacheKey);

                // 7. Save Reservation
                return reservationRepository.save(Reservation.builder()
                        .reservationStatus(ReservationStatus.BOOKED)
                        .seatsReserved(seats)
                        .show(show)
                        .user(userRepository.findByUsername(currentUserName).get())
                        .amountPaid(reservationRequestDto.getAmount())
                        .createdAt(LocalDateTime.now())
                        .build());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Lock acquisition interrupted", e);
            } finally {
                // 8. Release all locks in the finally block
                acquiredLocks.forEach(lock -> {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
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
    public Reservation cancelReservation(long reservationId) {
        return reservationRepository.findById(reservationId)
                .map(reservationIdb -> {
                    if (LocalDateTime.now().isAfter(reservationIdb.getShow().getStartTime()))
                        throw new ShowStartedException(SHOW_STARTED_EXCEPTION, HttpStatus.BAD_REQUEST);

                    reservationIdb.getSeatsReserved()
                            .forEach(seat -> {
                                seat.setStatus(SeatStatus.UNBOOKED);
                                seatRepository.save(seat);
                            });
                     //Hard Delete
                     //But soft delete allows us to see the transactions (as of now) but might seem cluttered as well
//                    reservationIdb.getSeatsReserved().clear();
//                    reservationRepository.delete(reservationIdb);

                    reservationIdb.setReservationStatus(ReservationStatus.CANCELED);

                    if(reservationIdb !=null) {
                    String key = "seats:show:" + reservationIdb.getShow().getShowId() ;
                    redisService.delete(key);
                    }

                    return reservationRepository.save(reservationIdb);
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
