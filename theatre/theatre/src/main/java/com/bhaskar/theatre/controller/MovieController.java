package com.bhaskar.theatre.controller;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
@RestController
@RequestMapping("/api/v1/movies")
public class MovieController {

    private final MovieService movieService;


}

