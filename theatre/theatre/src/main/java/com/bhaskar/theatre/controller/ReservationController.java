package com.bhaskar.theatre.controller;


import com.bhaskar.theatre.dto.ApiResponseDto;
import com.bhaskar.theatre.dto.PagedApiResponseDto;
import com.bhaskar.theatre.dto.ReservationRequestDto;
import com.bhaskar.theatre.entity.Reservation;
import com.bhaskar.theatre.service.ReservationService;
import jakarta.persistence.GeneratedValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @Autowired
    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/user/all")
    public ResponseEntity<PagedApiResponseDto> getAllReservationsForCurrentUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        PagedApiResponseDto response =
                reservationService.getAllReservationsForCurrentUser(page, size);

        return ResponseEntity.ok(response);
    }

    @Secured({"ROLE_ADMIN", "ROLE_SUPER_ADMIN"})
    @GetMapping("/filter")
    public ResponseEntity<PagedApiResponseDto> filterReservations(
            @RequestParam(required = false) long theaterId,
            @RequestParam(required = false) long movieId,
            @RequestParam(required = false) long userId,
            @RequestParam(defaultValue = "BOOKED") String reservationStatus,
            @RequestParam(required = false) String createdDate
    ){

        return null;
    }


    @PostMapping("/reserve")
    public ResponseEntity<ApiResponseDto> createReservation(
            @RequestBody ReservationRequestDto reservationRequestDto
    ){
        String currentUserName = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Reservation reservation = reservationService.createReservation(reservationRequestDto, currentUserName);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponseDto.builder()
                                .data(reservation)
                                .message("Reservation created with id: " + reservation.getId())
                                .build()
                );
    }

    @PostMapping("/cancel/{reservationId}")
    public ResponseEntity<ApiResponseDto> cancelReservation(@PathVariable long reservationId){
        Reservation reservation = reservationService.cancelReservation(reservationId);
        return ResponseEntity.ok(
                ApiResponseDto.builder()
                        .message("Reservation Canceled")
                        .data(reservation)
                        .build()
        );
    }



}
