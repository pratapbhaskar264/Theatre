package com.bhaskar.theatre.service;

import com.bhaskar.theatre.dto.ShowRequestDto;
import com.bhaskar.theatre.entity.Seat;
import com.bhaskar.theatre.entity.Show;
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

    @Autowired
    public ShowService(ShowRepository showRepository, MovieRepository movieRepository, TheatreRespository theatreRepository, SeatService seatService) {
        this.showRepository = showRepository;
        this.movieRepository = movieRepository;
        this.theatreRepository = theatreRepository;
        this.seatService = seatService;
    }

    public Page<Show> getllShows(int page, int size) {
        return showRepository.findAll(PageRequest.of(page, size));
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

    public void deleteShowById(long showId) {
        showRepository.deleteById(showId);
    }

//    @Transactional
public Show createNewShow(ShowRequestDto showRequestDto) {
    return movieRepository.findById(showRequestDto.getMovieId())
            .map(movie -> theatreRepository.findById(showRequestDto.getTheatreId()) // Changed to theatre
                    .map(theatre -> { // Changed to theatre
                        List<Seat> seats = new ArrayList<>();
                        showRequestDto.getSeats()
                                .forEach(seatStructure ->
                                        seats.addAll(
                                                seatService.createSeatsWithGivenPrice(
                                                        seatStructure.getSeatCount(),
                                                        seatStructure.getSeatPrice(),
                                                        seatStructure.getArea()
                                                )
                                        )
                                );

                        Show show = Show.builder()
                                .movie(movie)
                                .theatre(theatre) // Changed to theatre
                                .startTime(LocalDateTime.parse(showRequestDto.getStartTime()))
                                .endTime(LocalDateTime.parse(showRequestDto.getEndTime()))
                                .seats(seats)
                                .build();
                        return showRepository.save(show);
                    })
                    .orElseThrow(() -> new TheatreNotFoundException(THEATRE_NOT_FOUND, HttpStatus.BAD_REQUEST))) // Changed to Theatre
            .orElseThrow(() -> new MovieNotFoundException(MOVIE_NOT_FOUND, HttpStatus.BAD_REQUEST));
}
}
