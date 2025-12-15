package com.bhaskar.theatre.repository;

import com.bhaskar.theatre.entity.Show;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;

@Repository
public interface ShowRepository extends JpaRepository<Show ,Long> {

    Page<Show> findByTheaterId(long theaterId, Pageable pageable);
    Page<Show> findByMovieId(long movieId, Pageable pageable);
    Page<Show> findByTheaterIdAndMovieId(long theaterId,long movieId, Pageable pageable);
}
