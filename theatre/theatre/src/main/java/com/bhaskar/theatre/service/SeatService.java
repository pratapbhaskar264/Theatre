package com.bhaskar.theatre.service;

import com.bhaskar.theatre.entity.Seat;
import com.bhaskar.theatre.entity.Show;
import com.bhaskar.theatre.enums.SeatStatus;
import com.bhaskar.theatre.exception.ShowNotFoundException;
import com.bhaskar.theatre.exception.ShowStartedException;
import com.bhaskar.theatre.repository.SeatRepository;
import com.bhaskar.theatre.repository.ShowRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.bhaskar.theatre.constant.ExceptionMessages.SHOW_NOT_FOUND;

@Service
public class SeatService {
    private final SeatRepository seatRepository;
    private final RedisService redisService;
    private final ShowRepository showRepository;

    @Autowired
    public SeatService(SeatRepository seatRepository, RedisService redisService, ShowRepository showRepository) {
        this.seatRepository = seatRepository;
        this.redisService = redisService;
        this.showRepository = showRepository;
    }

    public List<Seat> createSeatsWithGivenPrice(int count, double price, String area) {
        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            seats.add(Seat.builder()
                    .number(i)
                    .area(area)
                    .price(price)
                    .status(SeatStatus.UNBOOKED)
                    // Don't call seatRepository.save() here!
                    .build());
        }
        return seats;
    }


    public List<Seat> getSeatsByShow(Long showId) {



        String key = "seats:show:" + showId;

        // Check Redis
        List<Seat> cachedSeats = redisService.get(key, List.class);
        if (cachedSeats != null) return cachedSeats;

        // This now matches the name your Controller/Service used before
        List<Seat> seats = seatRepository.findByShowId(showId);

        if (!seats.isEmpty()) {
            redisService.set(key, seats, 10L);
        }
        return seats;
    }
}
