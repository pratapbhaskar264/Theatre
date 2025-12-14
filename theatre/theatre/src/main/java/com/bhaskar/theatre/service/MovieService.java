package com.bhaskar.theatre.service;

import com.bhaskar.theatre.dto.MovieRequestDto;
import com.bhaskar.theatre.entity.Movie;
import org.springframework.data.domain.Page;

public class MovieService {
    public Page<Movie> getAllMovies(int page, int pageSize) {
        return null;
    }

    public Movie getMovieById(long movieId) {
        return null;
    }

    public Movie createNewMovie(MovieRequestDto movieRequestDto) {
        return null;
    }

    public Movie updateMovieById(long movieId, MovieRequestDto movieRequestDto) {
        return null;
    }


    public void deleteMovieById(long movieId) {
        return;
    }
}
