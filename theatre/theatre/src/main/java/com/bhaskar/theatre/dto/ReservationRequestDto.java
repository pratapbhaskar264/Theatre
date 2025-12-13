package com.bhaskar.theatre.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ReservationRequestDto {

    private long showId;
    private List<Long> seatIdsReserve;
    private double amount;

}
