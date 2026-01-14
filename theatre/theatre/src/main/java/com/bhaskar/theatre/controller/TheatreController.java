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
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/theatres")
public class TheatreController {

    private final TheatreService theatreService;

    @Autowired
    public TheatreController(TheatreService theatreService) {
        this.theatreService = theatreService;
    }

    @GetMapping("/all")
    public ResponseEntity<PagedApiResponseDto> getAllTheatres(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Page<Theatre> theatrePage = theatreService.getAllTheatres(page, size);
        return ResponseEntity.ok(
                PagedApiResponseDto.builder()
                        .totalPages(theatrePage.getTotalPages())
                        .totalElements(theatrePage.getTotalElements())
                        .currentCount(theatrePage.getNumberOfElements())
                        .currentPageData(theatrePage.getContent())
                        .build()
        );
    }

    @GetMapping("/location/{location}")
    public ResponseEntity<PagedApiResponseDto> getAllTheatresByLocation(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @PathVariable String location
    ){
        Page<Theatre> theatrePage = theatreService.getAllTheatresByLocation(page, size, location);
        return ResponseEntity.ok(
                PagedApiResponseDto.builder()
                        .totalPages(theatrePage.getTotalPages())
                        .totalElements(theatrePage.getTotalElements())
                        .currentCount(theatrePage.getNumberOfElements())
                        .currentPageData(theatrePage.getContent())
                        .build()
        );
    }

//    @Secured({"ROLE_ADMIN"})
    @GetMapping("/theatre/{theatreId}")
    public ResponseEntity<ApiResponseDto> getTheatreById(@PathVariable long theatreId){
        Theatre theatre = theatreService.getTheatreById(theatreId);
        return ResponseEntity.ok(
                ApiResponseDto.builder()
                        .data(theatre)
                        .message("Fetched theatre by id: " + theatre.getId())
                        .build()
        );
    }

//    @Secured({"ROLE_ADMIN"})
    @PostMapping("/theatre/create")
    public ResponseEntity<ApiResponseDto> createTheatre(@RequestBody TheatreRequestDto theatreRequestDto){
        Theatre theatre = theatreService.createNewTheatre(theatreRequestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponseDto.builder()
                                .message("New Theatre created with id: " + theatre.getId())
                                .data(theatre)
                                .build()
                );
    }

//    @Secured({"ROLE_ADMIN"})
    @PutMapping("/theatre/update/{theatreId}")
    public ResponseEntity<ApiResponseDto> updateTheatreById(@PathVariable long theatreId, @RequestBody TheatreRequestDto theatreRequestDto){
        Theatre updatedTheatre = theatreService.updateTheatreById(theatreId, theatreRequestDto);
        return ResponseEntity.ok(
                ApiResponseDto.builder()
                        .message("Theatre updated")
                        .data(updatedTheatre)
                        .build()
        );
    }

//    @Secured({"ROLE_ADMIN"})
    @Secured({"ROLE_SUPER_ADMIN","ROLE_ADMIN"})
    @DeleteMapping("/theatre/delete/{theatreId}")
    public ResponseEntity<?> deleteTheatreById(@PathVariable long theatreId){
        theatreService.deleteTheatreById(theatreId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}