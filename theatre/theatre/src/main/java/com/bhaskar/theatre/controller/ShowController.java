package com.bhaskar.theatre.controller;

import com.bhaskar.theatre.dto.PagedApiResponseDto;
import com.bhaskar.theatre.entity.Show;
import com.bhaskar.theatre.service.ShowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shows")
public class ShowController {

    private final ShowService showService;

    @Autowired
    public ShowController(ShowService showService) {
        this.showService = showService;
    }


    @GetMapping("/all")
    public ResponseEntity<PagedApiResponseDto> getAllShows(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Page<Show> showPage = showService.getllShows(page, size);
        return ResponseEntity.ok(
                PagedApiResponseDto.builder()
                        .currentCount(showPage.getNumberOfElements())
                        .currentPageData(showPage.getContent())
                        .totalElements(showPage.getTotalElements())
                        .totalPages(showPage.getTotalPages())
                        .build()
        );
    }
    @GetMapping("/filter")
    public ResponseEntity<PagedApiResponseDto> filterShows(
            @RequestParam(required = false) Long theaterId,
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) String showDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Page<Show> showPage = showService.filterShowsByTheaterIdAndMovieId(theaterId, movieId, PageRequest.of(page, size));
        return ResponseEntity.ok(
                PagedApiResponseDto.builder()
                        .currentCount(showPage.getNumberOfElements())
                        .currentPageData(showPage.getContent())
                        .totalElements(showPage.getTotalElements())
                        .totalPages(showPage.getTotalPages())
                        .build()
        );
    }




}
