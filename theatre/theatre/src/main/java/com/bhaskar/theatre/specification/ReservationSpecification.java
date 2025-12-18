package com.bhaskar.theatre.specification;

import com.bhaskar.theatre.entity.Reservation;
import com.bhaskar.theatre.enums.ReservationStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReservationSpecification {

    public static Specification<Reservation> hasTheaterId(Long theaterId) {
        return (root, query, cb) ->
                theaterId == null ? null :
                        cb.equal(root.get("show")
                                .get("theater")
                                .get("id"), theaterId);
    }

    public static Specification<Reservation> hasMovieId(Long movieId) {
        return (root, query, cb) ->
                movieId == null ? null :
                        cb.equal(root.get("show")
                                .get("movie")
                                .get("id"), movieId);
    }

    public static Specification<Reservation> hasUserId(Long userId) {
        return (root, query, cb) ->
                userId == null ? null :
                        cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Reservation> hasStatus(ReservationStatus status) {
        return (root, query, cb) ->
                status == null ? null :
                        cb.equal(root.get("reservationStatus"), status);
    }

    public static Specification<Reservation> createdOn(String createdDate) {
        return (root, query, cb) -> {
            if (createdDate == null) return null;

            LocalDate date = LocalDate.parse(createdDate);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(23, 59, 59);

            return cb.between(root.get("createdAt"), start, end);
        };
    }
}
