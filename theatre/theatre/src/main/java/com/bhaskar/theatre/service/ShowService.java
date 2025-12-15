package com.bhaskar.theatre.service;

import com.bhaskar.theatre.entity.Show;
import com.bhaskar.theatre.repository.MovieRepository;
import com.bhaskar.theatre.repository.ShowRepository;
import com.bhaskar.theatre.repository.TheatreRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

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
}
