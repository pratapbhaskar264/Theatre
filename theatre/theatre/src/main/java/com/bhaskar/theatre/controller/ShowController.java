package com.bhaskar.theatre.controller;

import com.bhaskar.theatre.dto.ApiResponseDto;
import com.bhaskar.theatre.dto.PagedApiResponseDto;
import com.bhaskar.theatre.dto.ShowRequestDto;
import com.bhaskar.theatre.dto.ShowTimingUpdateDto;
import com.bhaskar.theatre.entity.Seat;
import com.bhaskar.theatre.entity.Show;
import com.bhaskar.theatre.entity.Theatre;
import com.bhaskar.theatre.exception.ShowTimingClashException;
import com.bhaskar.theatre.repository.ShowRepository;
import com.bhaskar.theatre.repository.TheatreRespository;
import com.bhaskar.theatre.service.ShowService;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static com.bhaskar.theatre.constant.ExceptionMessages.TIMING_CLASH;

@RestController
@RequestMapping("/api/v1/shows")
public class ShowController {

    private final ShowService showService;
    private final TheatreRespository theatreRespository;
    private final ShowRepository showRepository;

    @Autowired
    public ShowController(ShowService showService, TheatreRespository theatreRespository, ShowRepository showRepository) {
        this.showService = showService;
        this.theatreRespository = theatreRespository;
        this.showRepository = showRepository;
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

    @GetMapping("/{showId}/seats")
    public ResponseEntity<ApiResponseDto> getShowSeatStructure(@PathVariable long showId) {
        // Fetch seats (this should check Redis first, then DB)
        List<Seat> seats = showService.getSeatsByShowId(showId);

        return ResponseEntity.ok(
                ApiResponseDto.builder()
                        .message("Seat structure fetched for show: " + showId)
                        .data(seats)
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

    @Secured({"ROLE_ADMIN", "ROLE_SUPER_ADMIN"})
    @GetMapping("/show/{showId}")
    public ResponseEntity<ApiResponseDto> getShowById(@PathVariable long showId){
        Show show = showService.getShowById(showId);
        return ResponseEntity.ok(
                ApiResponseDto.builder()
                        .data(show)
                        .message("Fetched show with id: " + show.getId())
                        .build()
        );
    }

    @Secured({"ROLE_ADMIN", "ROLE_SUPER_ADMIN"})
    @PostMapping("/show/create")
    public ResponseEntity<ApiResponseDto> createShow(@RequestBody ShowRequestDto showRequestDto){
        Theatre theatre = theatreRespository.findById(showRequestDto.getTheatreId())
                .orElseThrow(() -> new RuntimeException("Theatre not found"));

        //fix this exists by and all

        boolean isClashing = showRepository.existsByTheatreAndStartTimeBeforeAndEndTimeAfter(
                theatre,
                LocalDateTime.parse(showRequestDto.getEndTime()),
                LocalDateTime.parse(showRequestDto.getStartTime())
        );

        if (isClashing) {
            throw new ShowTimingClashException(TIMING_CLASH,HttpStatus.BAD_REQUEST);
        }
        Show show = showService.createNewShow(showRequestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponseDto.builder()
                                .message("Show created with id: " + show.getId())
                                .data(show)
                                .build()
                );
    }
//    @Secured({"ROLE_ADMIN", "ROLE_SUPER_ADMIN"})
//    @PatchMapping("/show/update/movie/{showId}")
//    public ResponseEntity<ApiResponseDto> updateMovie(
//            @PathVariable long showId,
//            @RequestParam long movieId
//    ) {
//        Show updatedShow = showService.updateShowMovie(showId, movieId);
//
//        return ResponseEntity.ok(
//                ApiResponseDto.builder()
//                        .data(updatedShow)
//                        .message("Movie updated for show " + showId)
//                        .build()
//        );
//    }

//    @Secured({"ROLE_ADMIN", "ROLE_SUPER_ADMIN"})
//    @PatchMapping("/show/update/theatre/{showId}")
//    public ResponseEntity<ApiResponseDto> updateTheatre(
//            @PathVariable long showId,
//            @RequestParam long theatreId
//    ) {
//        Show updatedShow = showService.updateShowTheatre(showId, theatreId);
//
//        return ResponseEntity.ok(
//                ApiResponseDto.builder()
//                        .data(updatedShow)
//                        .message("Theatre updated for show " + showId)
//                        .build()
//        );
//    }

//    @Secured({"ROLE_ADMIN", "ROLE_SUPER_ADMIN"})
//    @PatchMapping("/show/update/timings/{showId}")
//    public ResponseEntity<ApiResponseDto> updateShowTimings(
//            @PathVariable long showId,
//            @RequestBody ShowTimingUpdateDto timingDto
//    ) {
//        Show updatedShow = showService.updateShowTimings(
//                showId,
//                timingDto.getStartTime(),
//                timingDto.getEndTime()
//        );
//
//        return ResponseEntity.ok(
//                ApiResponseDto.builder()
//                        .data(updatedShow)
//                        .message("Show timings updated for show " + showId)
//                        .build()
//        );
//    }

    @Secured({"ROLE_ADMIN", "ROLE_SUPER_ADMIN"})
    @DeleteMapping("/show/delete/{showId}")
    public ResponseEntity<?> deleteShowById(@PathVariable long showId){
        showService.deleteShowById(showId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


}
