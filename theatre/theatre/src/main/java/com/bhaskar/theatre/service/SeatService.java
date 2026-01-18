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
    private final RedisService redisService;

    @Autowired
    public SeatService(SeatRepository seatRepository, RedisService redisService) {
        this.seatRepository = seatRepository;
        this.redisService = redisService;
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


    public List<Seat> getSeatsByShow(Long showId) {
        String key = "seats:show:" + showId;

        List<Seat> cachedSeats = redisService.get(key, List.class);
        if (cachedSeats != null) {
            return cachedSeats;
        }

        List<Seat> seats = seatRepository.findByShowId(showId);

        if (!seats.isEmpty()) {
            redisService.set(key, seats, 10L);
        }

        return seats;
    }
}
