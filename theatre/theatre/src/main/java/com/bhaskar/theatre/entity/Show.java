package com.bhaskar.theatre.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Show {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    @ManyToOne(targetEntity = Movie.class)
            @JoinColumn(referencedColumnName = "id" , nullable = false)
    Movie movie;

    @ManyToOne()
    Theatre theatre;
    LocalDateTime starTime;
    LocalDateTime endTime;

    @OneToMany(fetch = FetchType.LAZY , cascade = CascadeType.REMOVE)
    List<Seat> seats;
}
