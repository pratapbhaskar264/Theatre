package com.bhaskar.theatre.service;

import com.bhaskar.theatre.dto.PagedApiResponseDto;
import com.bhaskar.theatre.dto.ReservationRequestDto;
import com.bhaskar.theatre.entity.Reservation;
import com.bhaskar.theatre.repository.ReservationRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;

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


    public PagedApiResponseDto getAllReservationsForCurrentUser(int page, int size) {
        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
        Pageable pageable = (Pageable) PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Page<Reservation> reservationPage =
                reservationRepository.findByUserUsername(username, pageable);

        // 4️⃣ Build paged response DTO
        return PagedApiResponseDto.builder()
                .content(reservationPage.getContent())
                .pageNumber(reservationPage.getNumber())
                .pageSize(reservationPage.getSize())
                .totalElements(reservationPage.getTotalElements())
                .totalPages(reservationPage.getTotalPages())
                .isLast(reservationPage.isLast())
                .build();
    }

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
                            throw new SeatLockAcquiredException(SEAT_LOCK_ACQUIRED, HttpStatus.CONFLICT);
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
}
