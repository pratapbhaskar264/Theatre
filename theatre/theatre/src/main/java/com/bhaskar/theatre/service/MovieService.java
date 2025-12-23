package com.bhaskar.theatre.service;

import com.bhaskar.theatre.dto.MovieRequestDto;
import com.bhaskar.theatre.entity.Movie;
import com.bhaskar.theatre.enums.MovieGenre;
import com.bhaskar.theatre.exception.MovieNotFoundException;
import com.bhaskar.theatre.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import static com.bhaskar.theatre.constant.ExceptionMessages.MOVIE_NOT_FOUND;


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
                                MOVIE_NOT_FOUND,
                                HttpStatus.NOT_FOUND
                        )
                );

    }

    public Movie createNewMovie(MovieRequestDto movieRequestDto) {

        Movie movie = Movie.builder()
                .movieLanguage(movieRequestDto.getMovieLanguage())
                .movieLength(movieRequestDto.getMovieLength())
                .genre(movieRequestDto.getGenre().stream().map(g -> MovieGenre.valueOf(g.toUpperCase()))
                .toList())
                .movieName(movieRequestDto.getMovieName())
                .releaseDate(LocalDate.parse(movieRequestDto.getReleaseDate()))
                .build();

        return movieRepository.save(movie);
    }

    public Movie updateMovieById(long movieId, MovieRequestDto movieRequestDto) {
        return movieRepository.findById(movieId)
                .map(movieInDb -> {
                    movieInDb.setMovieName(movieRequestDto.getMovieName());
                    movieInDb.setGenre(movieRequestDto.getGenre().stream().map(MovieGenre::valueOf).toList());
                    movieInDb.setMovieLanguage(movieRequestDto.getMovieLanguage());
                    movieInDb.setReleaseDate(LocalDate.parse(movieRequestDto.getReleaseDate()));
                    movieInDb.setMovieLength(movieRequestDto.getMovieLength());

                    return movieRepository.save(movieInDb);
                })
                .orElseThrow(() -> new MovieNotFoundException(MOVIE_NOT_FOUND, HttpStatus.NOT_FOUND));
    }


    public void deleteMovieById(long movieId) {
        movieRepository.deleteById(movieId);
    }
}
