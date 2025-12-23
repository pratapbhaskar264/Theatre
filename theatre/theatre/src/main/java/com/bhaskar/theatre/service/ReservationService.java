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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    }
//
//
//    public PagedApiResponseDto getAllReservationsForCurrentUser(int page, int size) {
//        String username = SecurityContextHolder
//                .getContext()
//                .getAuthentication()
//                .getName();
//        Pageable pageable = (Pageable) PageRequest.of(
//                page,
//                size,
//                Sort.by("createdAt").descending()
//        );
//
//        Page<Reservation> reservationPage =
//                reservationRepository.findByUserUsername(username, (java.awt.print.Pageable) pageable);
//
//        // 4️⃣ Build paged response DTO
//        return PagedApiResponseDto.builder()
//                .content(reservationPage.getContent())
//                .pageNumber(reservationPage.getNumber())
//                .pageSize(reservationPage.getSize())
//                .totalElements(reservationPage.getTotalElements())
//                .totalPages(reservationPage.getTotalPages())
//                .isLast(reservationPage.isLast())
//                .build();
//    }

    @Transactional
    public Reservation createReservation(ReservationRequestDto reservationRequestDto, String currentUserName) {

        return showRepository
                .findById(reservationRequestDto.getShowId())
                .map(show -> {
                    List<Seat> seats = reservationRequestDto
                            .getSeatIdsToReserve()
                            .stream()
                            .map(seatRepository::findById)
                            .map(Optional::get)
                            .toList();

                    // Calculate the amount to be paid.
                    Double amountToBePaid = seats.stream().map(Seat::getPrice).reduce(0.0, Double::sum);

                    if(reservationRequestDto.getAmount() != amountToBePaid)
                        throw new AmountNotMatchException(AMOUNT_NOT_MATCH, HttpStatus.BAD_REQUEST);

                    // Acquire the lock for all seats
                    seats.forEach(seat -> {
                        ReentrantLock seatLock = seatLockManager.getLockForSeat(seat.getId());
                        boolean isLockFree = seatLock.tryLock();
                        if (!isLockFree){
                            throw new SeatLockAccquiredException(SEAT_LOCK_ACCQUIRED, HttpStatus.CONFLICT);
                        }
                    });

                    boolean anyBookedSeat = seats.stream().map(Seat::getStatus).anyMatch(seatStatus -> seatStatus.equals(SeatStatus.BOOKED));

                    if (anyBookedSeat){
                        // Remove lock for every seat
                        seats.forEach(seat -> seatLockManager.removeLockForSeat(seat.getId()));
                        throw new SeatAlreadyBookedException(SEAT_ALREADY_BOOKED, HttpStatus.BAD_REQUEST);
                    }

                    // Mark all the seats as booked
                    List<Seat> bookedSeats = seats.stream().map(seat -> {
                        seat.setStatus(SeatStatus.BOOKED);
                        return seatRepository.save(seat);
                    }).toList();


                    // Create the reservation
                    Reservation reservation = Reservation.builder()
                            .reservationStatus(ReservationStatus.BOOKED)
                            .seatsReserved(bookedSeats)
                            .show(show)
                            .user(userRepository.findByUsername(currentUserName).get())
                            .amountPaid(reservationRequestDto.getAmount())
                            .createdAt(LocalDateTime.now())
                            .build();

                    // Remove lock for every seat
                    seats.forEach(seat -> seatLockManager.removeLockForSeat(seat.getId()));

                    return reservationRepository.save(reservation);
                })
                .orElseThrow(() -> new ShowNotFoundException(SHOW_NOT_FOUND, HttpStatus.BAD_REQUEST));
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
//    public PagedApiResponseDto filterReservations(
//            Long theaterId,
//            Long movieId,
//            Long userId,
//            String reservationStatus,
//            String createdDate,
//            int page,
//            int size
//    ) {
//
//        ReservationStatus status = ReservationStatus.valueOf(reservationStatus);
//
//        Specification<Reservation> specification =
//                Specification.where(ReservationSpecification.hasTheaterId(theaterId))
//                        .and(ReservationSpecification.hasMovieId(movieId))
//                        .and(ReservationSpecification.hasUserId(userId))
//                        .and(ReservationSpecification.hasStatus(status))
//                        .and(ReservationSpecification.createdOn(createdDate));
//
//        Pageable pageable = PageRequest.of(
//                page,
//                size,
//                Sort.by("createdAt").descending()
//        );
//
//        Page<Reservation> reservationPage =
//                reservationRepository.findAll(specification, pageable);
//
//        return PagedApiResponseDto.builder()
//                .content(reservationPage.getContent())
//                .pageNumber(reservationPage.getNumber())
//                .pageSize(reservationPage.getSize())
//                .totalElements(reservationPage.getTotalElements())
//                .totalPages(reservationPage.getTotalPages())
//                .isLast(reservationPage.isLast())
//                .build();
//    }

}
