package com.bhaskar.theatre.entity;

import com.bhaskar.theatre.enums.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private User user;

    @JsonIgnore // <--- ADD THIS LINE HERE
    @ManyToOne
    @JoinColumn(name = "show_id")
    private Show show;

    @OneToMany(fetch = FetchType.EAGER)
    private List<Seat> seatsReserved;
    private double amountPaid;

    @Enumerated(value = EnumType.STRING)
    private ReservationStatus reservationStatus;

    private LocalDateTime createdAt;


}
