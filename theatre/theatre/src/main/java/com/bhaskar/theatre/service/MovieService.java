package com.bhaskar.theatre.service;

import com.bhaskar.theatre.dto.MovieRequestDto;
import com.bhaskar.theatre.entity.Movie;
import com.bhaskar.theatre.exception.MovieNotFoundException;
import com.bhaskar.theatre.constant.ExceptionMessages;
import com.bhaskar.theatre.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


@Service
public class MovieService {

    private final MovieRepository movieRepository;

    @Autowired
    public MovieService(MovieRepository movieRepository){
        this.movieRepository = movieRepository;
    }


    public Page<Movie> getAllMovies(int page, int pageSize) {
        return movieRepository.findAll(PageRequest.of(page,pageSize)) ;
    }

    public Movie getMovieById(long movieId) {
        return movieRepository.findById(movieId)
                .orElseThrow(() ->
                        new MovieNotFoundException(
                                ExceptionMessages.MOVIE_NOT_FOUND,
                                HttpStatus.NOT_FOUND
                        )
                );

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
