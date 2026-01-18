package com.bhaskar.theatre.service;

import com.bhaskar.theatre.dto.MovieRequestDto;
import com.bhaskar.theatre.entity.Movie;
import com.bhaskar.theatre.enums.MovieGenre;
import com.bhaskar.theatre.exception.MovieNotFoundException;
import com.bhaskar.theatre.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static com.bhaskar.theatre.constant.ExceptionMessages.MOVIE_NOT_FOUND;


@Service
public class MovieService {

    private final MovieRepository movieRepository;
    private final RedisService redisService;

    @Autowired
    public MovieService(MovieRepository movieRepository, RedisService redisService){
        this.movieRepository = movieRepository;
        this.redisService = redisService;
    }

    public Page<Movie> getAllMovies(int page, int pageSize) {
        String cacheKey = "movies:all:p" + page + ":s" + pageSize;

        List<Movie> cachedContent = redisService.get(cacheKey, List.class);

        if (cachedContent != null) {
            return new PageImpl<>(cachedContent, PageRequest.of(page, pageSize), cachedContent.size());
        }

        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Movie> moviePage = movieRepository.findAll(pageable);

        if (!moviePage.isEmpty()) {
            redisService.set(cacheKey, moviePage.getContent(), 60L);
        }

        return moviePage;
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
        redisService.deleteByPattern("movies:all:");
        return movieRepository.save(movie);
    }
//    @Transactional
//    public Movie updateMovie(Long id, MovieRequestDto dto) {
//        // 1. Update DB
//        Movie updatedMovie = movieRepository.findById(id)
//                .map(movie -> {
//                    movie.setTitle(dto.getTitle());
//                    // ... other fields
//                    return movieRepository.save(movie);
//                }).orElseThrow();
//
//        // 2. Clear the LIST cache (all pages)
//        redisService.deleteByPattern("movies:all:");
//
//        // 3. Clear the INDIVIDUAL movie cache
//        redisService.delete("movie:" + id);
//
//        return updatedMovie;
//    }
    public Movie updateMovieById(long movieId, MovieRequestDto movieRequestDto) {


        Movie updatedMovie = movieRepository.findById(movieId)
                .map(movieInDb -> {
                    movieInDb.setMovieName(movieRequestDto.getMovieName());
                    movieInDb.setGenre(movieRequestDto.getGenre().stream().map(MovieGenre::valueOf).toList());
                    movieInDb.setMovieLanguage(movieRequestDto.getMovieLanguage());
                    movieInDb.setReleaseDate(LocalDate.parse(movieRequestDto.getReleaseDate()));
                    movieInDb.setMovieLength(movieRequestDto.getMovieLength());

                    return movieRepository.save(movieInDb);
                });

                if(updatedMovie!=null) {
                    redisService.deleteByPattern("movies:all:");

                    redisService.delete("movie:" + movieId);

                }

                if(updatedMovie!=null){
                    return updatedMovie;
                }
                else{
                    throw new MovieNotFoundException(MOVIE_NOT_FOUND,HttpStatus.NOT_FOUND);
                }
    }


    public void deleteMovieById(long movieId) {
        redisService.deleteByPattern("movies:all:");

        redisService.delete("movie:" + movieId);
        movieRepository.deleteById(movieId);
    }
}
