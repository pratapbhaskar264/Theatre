package com.bhaskar.theatre.entity;

import com.bhaskar.theatre.enums.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;


@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    @ManyToOne
    @ToString.Exclude
    private User user;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "show_id")
    @ToString.Exclude
    private Show show;

    @OneToMany(fetch = FetchType.EAGER)
    @ToString.Exclude
    private List<Seat> seatsReserved;
    private double amountPaid;

    @Enumerated(value = EnumType.STRING)
    private ReservationStatus reservationStatus;

    private LocalDateTime createdAt;


}
