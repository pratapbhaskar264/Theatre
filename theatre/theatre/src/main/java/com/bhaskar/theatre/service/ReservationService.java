package com.bhaskar.theatre.service;



import com.bhaskar.theatre.dto.ReservationRequestDto;
import com.bhaskar.theatre.entity.Reservation;
import com.bhaskar.theatre.entity.Seat;
import com.bhaskar.theatre.enums.ReservationStatus;
import com.bhaskar.theatre.enums.SeatStatus;
import com.bhaskar.theatre.exception.*;
import com.bhaskar.theatre.repository.ReservationRepository;
import com.bhaskar.theatre.repository.SeatRepository;
import com.bhaskar.theatre.repository.ShowRepository;
import com.bhaskar.theatre.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

import static com.bhaskar.theatre.constant.ExceptionMessages.*;

@Service
public class ReservationService {

    private final SeatLockManager seatLockManager;
    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final ShowRepository showRepository;
    private final UserRepository userRepository;

    @Autowired
    public ReservationService(SeatLockManager seatLockManager,
                              ReservationRepository reservationRepository,
                              SeatRepository seatRepository,
                              ShowRepository showRepository,
                              UserRepository userRepository) {
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
    }
    @Transactional
    public Reservation createReservation(ReservationRequestDto reservationRequestDto, String currentUserName) {
        return showRepository.findById(reservationRequestDto.getShowId()).map(show -> {

            List<Seat> seats = reservationRequestDto.getSeatIdsReserve().stream()
                    .map(seatRepository::findById)
                    .map(Optional::get)
                    .toList();

            // 1. Logic checks first (Amount check)
            Double amountToBePaid = seats.stream().map(Seat::getPrice).reduce(0.0, Double::sum);
            if(reservationRequestDto.getAmount() != amountToBePaid)
                throw new AmountNotMatchException(AMOUNT_NOT_MATCH, HttpStatus.BAD_REQUEST);

            List<Seat> lockedSeats = new ArrayList<>();
            try {
                // 2. Acquire Locks
                for (Seat seat : seats) {
                    ReentrantLock seatLock = seatLockManager.getLockForSeat(seat.getId());
                    if (seatLock.tryLock()) {
                        lockedSeats.add(seat);
                    } else {
                        throw new SeatLockAccquiredException(SEAT_LOCK_ACCQUIRED, HttpStatus.CONFLICT);
                    }
                }

                boolean anyBookedSeat = seats.stream()
                        .anyMatch(s -> s.getStatus().equals(SeatStatus.BOOKED));

                if (anyBookedSeat) {
                    throw new SeatAlreadyBookedException(SEAT_ALREADY_BOOKED, HttpStatus.BAD_REQUEST);
                }

                seats.forEach(seat -> {
                    seat.setStatus(SeatStatus.BOOKED);
                    seatRepository.save(seat);
                });

                return reservationRepository.save(Reservation.builder()
                        .reservationStatus(ReservationStatus.BOOKED)
                        .seatsReserved(seats)
                        .show(show)
                        .user(userRepository.findByUsername(currentUserName).get())
                        .amountPaid(reservationRequestDto.getAmount())
                        .createdAt(LocalDateTime.now())
                        .build());

            } finally {
                lockedSeats.forEach(seat -> {
                    ReentrantLock lock = seatLockManager.getLockForSeat(seat.getId());
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                    // Optional: seatLockManager.removeLockForSeat(seat.getId());
                });
            }
        }).orElseThrow(() -> new ShowNotFoundException(SHOW_NOT_FOUND, HttpStatus.BAD_REQUEST));
    }

    public Reservation getReservationById(long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(RESERVATION_NOT_FOUND, HttpStatus.NOT_FOUND));

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

                    reservationIdb.setReservationStatus(ReservationStatus.CANCELED);
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
