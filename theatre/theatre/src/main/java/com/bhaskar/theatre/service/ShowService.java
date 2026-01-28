package com.bhaskar.theatre.service;

import com.bhaskar.theatre.dto.ShowRequestDto;
import com.bhaskar.theatre.entity.Movie;
import com.bhaskar.theatre.entity.Seat;
import com.bhaskar.theatre.entity.Show;
import com.bhaskar.theatre.entity.Theatre;
import com.bhaskar.theatre.exception.MovieNotFoundException;
import com.bhaskar.theatre.exception.ShowNotFoundException;
import com.bhaskar.theatre.exception.TheatreNotFoundException;
import com.bhaskar.theatre.repository.MovieRepository;
import com.bhaskar.theatre.repository.ShowRepository;
import com.bhaskar.theatre.repository.TheatreRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.bhaskar.theatre.constant.ExceptionMessages.*;

@Service
public class ShowService {

    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final TheatreRespository theatreRepository;
    private final SeatService seatService;
    private final RedisService redisService;

    @Autowired
    public ShowService(ShowRepository showRepository, MovieRepository movieRepository, TheatreRespository theatreRepository, SeatService seatService, RedisService redisService) {
        this.showRepository = showRepository;
        this.movieRepository = movieRepository;
        this.theatreRepository = theatreRepository;
        this.seatService = seatService;
        this.redisService = redisService;
    }

    public Page<Show> getllShows(int page, int size) {
        return showRepository.findAll(PageRequest.of(page, size));
    }
    public List<Seat> getSeatsByShowId(long showId) {
        String cacheKey = "seats:show:" + showId;

        // 1. Try Redis
        List<Seat> cachedSeats = redisService.get(cacheKey, List.class);
        if (cachedSeats != null) {
            return cachedSeats;
        }

        // 2. Fallback to DB
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ShowNotFoundException(SHOW_NOT_FOUND,HttpStatus.NOT_FOUND) );

        List<Seat> seats = show.getSeats();

        // 3. Cache it for 30 minutes
        if (!seats.isEmpty()) {
            redisService.set(cacheKey, seats, 30L);
        }

        return seats;
    }
    public Page<Show> filterShowsByTheaterIdAndMovieId(Long theaterId, Long movieId, PageRequest pageRequest) {
        if(theaterId == null && movieId == null){
            return showRepository.findAll(pageRequest);
        } else if(theaterId == null){
            return showRepository.findByMovieId(movieId,  pageRequest);
        }
        return showRepository.findByTheatreIdAndMovieId(theaterId, movieId,  pageRequest);
    }


    public Show getShowById(long showId) {
        return showRepository.findById(showId)
                .orElseThrow(() -> new ShowNotFoundException(SHOW_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    @Transactional
    public void deleteShowById(long showId) {
        // 1. Define the cache key
        String cacheKey = "seats:show:" + showId;

        // 2. Clear from Redis first
        redisService.delete(cacheKey);

        // 3. Delete from DB (Seats will be deleted via CascadeType.ALL)
        showRepository.deleteById(showId);
    }

    @Transactional // This is mandatory for cascading to work
    public Show createNewShow(ShowRequestDto showRequestDto) {
        // 1. Fetch Movie and Theatre
        Movie movie = movieRepository.findById(showRequestDto.getMovieId())
                .orElseThrow(() -> new MovieNotFoundException(MOVIE_NOT_FOUND, HttpStatus.BAD_REQUEST));

        Theatre theatre = theatreRepository.findById(showRequestDto.getTheatreId())
                .orElseThrow(() -> new TheatreNotFoundException(THEATRE_NOT_FOUND, HttpStatus.BAD_REQUEST));

        // 2. Build the Show object (ID is still null here)
        Show show = Show.builder()
                .movie(movie)
                .theatre(theatre)
                .startTime(LocalDateTime.parse(showRequestDto.getStartTime()))
                .endTime(LocalDateTime.parse(showRequestDto.getEndTime()))
                .build();

        // 3. Generate and Link Seats
        List<Seat> allGeneratedSeats = new ArrayList<>();
        showRequestDto.getSeats().forEach(structure -> {
            List<Seat> seats = seatService.createSeatsWithGivenPrice(
                    structure.getSeatCount(),
                    structure.getSeatPrice(),
                    structure.getArea()
            );

            // LINKING STEP: This is what prevents the Transient error
            for (Seat seat : seats) {
                seat.setShow(show);
            }
            allGeneratedSeats.addAll(seats);
        });

        // 4. Attach the list to the Show
        show.setSeats(allGeneratedSeats);

        // 5. Save the Show.
        // Because of CascadeType.ALL, Hibernate will:
        // a) Save Show -> get generated ID
        // b) Save all Seats using that new ID automatically
        return showRepository.save(show);
    }
}
