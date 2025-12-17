package com.bhaskar.theatre.service;

import com.bhaskar.theatre.entity.Seat;
import com.bhaskar.theatre.enums.SeatStatus;
import com.bhaskar.theatre.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.IntStream;

@Service
public class SeatService {
    private final SeatRepository seatRepository;

    @Autowired
    public SeatService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    public List<Seat> createSeatsWithGivenPrice(int seats, double price, String area) {
        return IntStream.range(1, seats + 1)
                .mapToObj(seatCount -> Seat.builder()
                        .price(price)
                        .number(seatCount)
                        .area(area)
                        .status(SeatStatus.UNBOOKED)
                        .build()
                )
                .map(seatRepository::save)
                .toList();
    }
}
