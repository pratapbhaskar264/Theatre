package com.bhaskar.theatre.repository;

import com.bhaskar.theatre.entity.Theatre;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;

@Repository
public interface TheatreRespository extends JpaRepository<Theatre, Long> {
    Page<Theatre> findAllByLocation(String location, Pageable pageable);
}
