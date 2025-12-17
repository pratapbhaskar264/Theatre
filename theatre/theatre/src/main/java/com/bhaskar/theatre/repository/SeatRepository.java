package com.bhaskar.theatre.repository;

import com.bhaskar.theatre.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatRepository extends JpaRepository<Seat ,Long> {
}
