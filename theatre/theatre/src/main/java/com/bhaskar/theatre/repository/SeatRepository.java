package com.bhaskar.theatre.repository;

import com.bhaskar.theatre.entity.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByShowId(Long showId);

    // Fetch and lock seats for booking to prevent double-booking
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id IN :seatIds AND s.show.id = :showId")
    List<Seat> findByIdInAndShowIdWithLock(List<Long> seatIds, Long showId);
}