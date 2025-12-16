package com.bhaskar.theatre.controller;


import com.bhaskar.theatre.dto.ApiResponseDto;
import com.bhaskar.theatre.dto.PagedApiResponseDto;
import com.bhaskar.theatre.dto.TheatreRequestDto;
import com.bhaskar.theatre.entity.Theatre;
import com.bhaskar.theatre.service.TheatreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/theaters")
public class TheatreController {

    private final TheatreService theatreService;

    @Autowired
    public TheatreController(TheatreService theatreService) {
        this.theatreService = theatreService;
    }

    @GetMapping("/all")
    public ResponseEntity<PagedApiResponseDto> getAllTheaters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Page<Theatre> theaterPage = theatreService.getAllTheatres(page, size);
        return ResponseEntity.ok(
                PagedApiResponseDto.builder()
                        .totalPages(theaterPage.getTotalPages())
                        .totalElements(theaterPage.getTotalElements())
                        .currentCount(theaterPage.getNumberOfElements())
                        .currentPageData(theaterPage.getContent())
                        .build()
        );
    }

    @GetMapping("/location/{location}")
    public ResponseEntity<PagedApiResponseDto> getAllTheatersByLocation(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @PathVariable String location
    ){
        Page<Theatre> theaterPage = theatreService. getAllTheatresByLocation(page, size, location);
        return ResponseEntity.ok(
                PagedApiResponseDto.builder()
                        .totalPages(theaterPage.getTotalPages())
                        .totalElements(theaterPage.getTotalElements())
                        .currentCount(theaterPage.getNumberOfElements())
                        .currentPageData(theaterPage.getContent())
                        .build()
        );
    }


    @GetMapping("/theater/{theaterId}")
    public ResponseEntity<ApiResponseDto> getTheaterById(@PathVariable long theaterId){
        Theatre theater = theatreService.getTheatreById(theaterId);
        return ResponseEntity.ok(
                ApiResponseDto.builder()
                        .data(theater)
                        .message("Fetched theater by id: " + theater.getId())
                        .build()
        );
    }

    @PostMapping("/theater/create")
    public ResponseEntity<ApiResponseDto> createTheater(@RequestBody TheatreRequestDto theaterRequestDto){
        Theatre theater = theatreService.createNewTheatre(theaterRequestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponseDto.builder()
                                .message("New Theater created with id: " + theater.getId())
                                .data(theater)
                                .build()
                );
    }

    @PutMapping("/theater/update/{theaterId}")
    public ResponseEntity<ApiResponseDto> updateTheaterById(@PathVariable long theaterId, @RequestBody TheatreRequestDto theaterRequestDto){
        Theatre updatedTheater = theatreService.updateTheatreById(theaterId, theaterRequestDto);
        return ResponseEntity.ok(
                ApiResponseDto.builder()
                        .message("Theater updated")
                        .data(updatedTheater)
                        .build()
        );
    }

    @DeleteMapping("/theater/delete/{theaterId}")
    public ResponseEntity<?> deleteTheaterById(@PathVariable long theaterId){
        theatreService.deleteTheatreById(theaterId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }







}
