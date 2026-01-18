package com.bhaskar.theatre.service;

import com.bhaskar.theatre.entity.Seat;
import com.bhaskar.theatre.enums.SeatStatus;
import com.bhaskar.theatre.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class SeatService {
    private final SeatRepository seatRepository;

    @Autowired
    public SeatService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    public List<Seat> createSeatsWithGivenPrice(int seats, double price, String area){
        List<Seat> seatsToSave = IntStream.rangeClosed(1, seats)
                .mapToObj(i -> Seat.builder()
                        .price(price)
                        .number(i)
                        .area(area)
                        .status(SeatStatus.UNBOOKED)
                        .build()
                )
                .toList();

        return seatRepository.saveAll(seatsToSave);
    }

}
