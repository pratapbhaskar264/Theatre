package com.bhaskar.theatre.entity;

import com.bhaskar.theatre.enums.MovieGenre;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movie {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    long id;
    String movieName;

    @Enumerated(value = EnumType.STRING)
    List<MovieGenre> genre;
    int movieLength;
    String movieLanguage;
    LocalDate releaseDate;

    @JsonIgnore
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Show> shows;
}
