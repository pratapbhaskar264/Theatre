package com.bhaskar.theatre.repository;

import com.bhaskar.theatre.entity.Reservation;
import com.bhaskar.theatre.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying; // Add this
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // Add this
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional; // Add this

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // Simple derived query for the user's history
    Page<Reservation> findByUserUsername(String username, Pageable pageable);

    // Advanced filtering query
    @Query("SELECT r FROM Reservation r WHERE " +
            "(:theaterId IS NULL OR r.show.theatre.id = :theaterId) AND " +
            "(:movieId IS NULL OR r.show.movie.id = :movieId) AND " +
            "(:userId IS NULL OR r.user.id = :userId) AND " +
            "(r.reservationStatus = :status)")
    Page<Reservation> filterReservations(
            Long theaterId,
            Long movieId,
            Long userId,
            ReservationStatus status,
            Pageable pageable);

    // FIX: Method to clean up reservations before deleting a show
    @Modifying
    @Transactional
    @Query("DELETE FROM Reservation r WHERE r.show.id = :showId")
    void deleteByShowId(@Param("showId") long showId);
}