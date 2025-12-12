package com.bhaskar.theatre.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
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
