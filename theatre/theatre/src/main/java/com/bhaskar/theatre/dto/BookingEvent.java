package com.bhaskar.theatre.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingEvent {
    private Long reservationId;
    private String username;
    private Long showId;
    private String movieName;
    private List<Long> seatIds;
    private Double amount;
    private String status;
}
