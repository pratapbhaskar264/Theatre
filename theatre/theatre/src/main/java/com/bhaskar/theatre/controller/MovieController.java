package com.bhaskar.theatre.controller;



import com.bhaskar.theatre.dto.PagedApiResponseDto;
import com.bhaskar.theatre.entity.Movie;
import com.bhaskar.theatre.service.MovieService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
@RestController
@RequestMapping("/api/v1/movies")
public class MovieController {

    private final MovieService movieService;


    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/all")
    public ResponseEntity<PagedApiResponseDto> getAllMovies( @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "10") int pageSize) {
        Page<Movie> moviePage = movieService.getAllMovies(page, pageSize);
        List<Movie> movies = moviePage.getContent();
        return ResponseEntity.ok(
                PagedApiResponseDto.builder()
                        .totalPages(moviePage.getTotalPages())
                        .totalElements(moviePage.getTotalElements())
                        .currentPageData(movies)
                        .build()
        );
    }

    @GetMapping("/movie/{movieId}")
}

