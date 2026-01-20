package com.bhaskar.theatre.repository;

import com.bhaskar.theatre.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    // This query ensures you only get seats that belong to the specific show
    @Query("SELECT s FROM Seat s WHERE s.id IN :ids AND s.show.id = :showId")
    List<Seat> findByIdsAndShowId(@Param("ids") List<Long> ids, @Param("showId") Long showId);
    @Query("SELECT s FROM Seat s WHERE s.show.id = :showId")
    List<Seat> findByShowId(@Param("showId") Long showId);
}
