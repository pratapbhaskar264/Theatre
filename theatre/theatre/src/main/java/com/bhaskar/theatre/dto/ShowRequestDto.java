package com.bhaskar.theatre.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ShowRequestDto {

    private long movieId;
    private long theatreId;
    private String startTime;
    private String endTime;
    private List<SeatStructure> seats;
}
