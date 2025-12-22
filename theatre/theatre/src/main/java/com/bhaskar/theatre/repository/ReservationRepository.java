package com.bhaskar.theatre.repository;

import com.bhaskar.theatre.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.awt.print.Pageable;

//public interface ReservationRepository
//        extends JpaRepository<Reservation, Long>,
//        JpaSpecificationExecutor<Reservation> {
//}


public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {

    Page<Reservation> findByUserUsername(String username, Pageable pageable);
}

