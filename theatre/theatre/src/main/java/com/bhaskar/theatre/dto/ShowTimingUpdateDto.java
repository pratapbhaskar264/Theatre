package com.bhaskar.theatre.dto;

import lombok.Data;

import java.time.LocalTime;

@Data
public class ShowTimingUpdateDto {
    private LocalTime startTime;
    private LocalTime endTime;
}

