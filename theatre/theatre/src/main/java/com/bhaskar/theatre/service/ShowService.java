package com.bhaskar.theatre.service;

import com.bhaskar.theatre.entity.Show;
import com.bhaskar.theatre.exception.ShowNotFoundException;
import com.bhaskar.theatre.repository.MovieRepository;
import com.bhaskar.theatre.repository.ShowRepository;
import com.bhaskar.theatre.repository.TheatreRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

import static com.bhaskar.theatre.constant.ExceptionMessages.SHOW_NOT_FOUND;

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
            return showRepository.findByMovieId(movieId, pageRequest);
        }
        return showRepository.findByTheaterIdAndMovieId(theaterId, movieId, pageRequest);
    }

    public Show updateShowMovie(long showId, long movieId) {

        Show show = showRepository.findById(showId)
                .orElseThrow(() ->
                        new ShowNotFoundException(SHOW_NOT_FOUND, HttpStatus.NOT_FOUND)
                );

        show.setMovie(
                movieRepository.findById(movieId)
                        .orElseThrow(() ->
                                new RuntimeException("Movie not found")
                        )
        );

        return showRepository.save(show);
    }

    public Show updateShowTheatre(long showId, long theatreId) {

        Show show = showRepository.findById(showId)
                .orElseThrow(() ->
                        new ShowNotFoundException(SHOW_NOT_FOUND, HttpStatus.NOT_FOUND)
                );

        show.setTheatre(
                theatreRepository.findById(theatreId)
                        .orElseThrow(() ->
                                new RuntimeException("Theatre not found")
                        )
        );

        return showRepository.save(show);
    }

    public Show updateShowTimings(long showId, LocalTime startTime, LocalTime endTime) {

        Show show = showRepository.findById(showId)
                .orElseThrow(() ->
                        new ShowNotFoundException(SHOW_NOT_FOUND, HttpStatus.NOT_FOUND)
                );

        show.setStartTime(startTime);
        show.setEndTime(endTime);

        return showRepository.save(show);
    }



    public Show getShowById(long showId) {
        return showRepository.findById(showId)
                .orElseThrow(() -> new ShowNotFoundException(SHOW_NOT_FOUND, HttpStatus.NOT_FOUND));
    }
}
