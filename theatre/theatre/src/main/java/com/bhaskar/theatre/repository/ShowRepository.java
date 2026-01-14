package com.bhaskar.theatre.repository;

import com.bhaskar.theatre.entity.Show;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
//@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {
    Page<Show> findByTheatreId(long theatreId, Pageable pageable);
    Page<Show> findByMovieId(long movieId, Pageable pageable);
    Page<Show> findByTheatreIdAndMovieId(long theatreId, long movieId, Pageable pageable);

    // ADD THIS METHOD:
    boolean existsByTheatreAndStartTimeBeforeAndEndTimeAfter(
            com.bhaskar.theatre.entity.Theatre theatre,
            java.time.LocalDateTime endTime,
            java.time.LocalDateTime startTime
    );
}
