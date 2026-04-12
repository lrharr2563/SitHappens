package com.sithappens.sithappens.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sithappens.sithappens.model.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Review findByBookingIdAndReviewerIdAndRevieweeId(Long bookingId, Long reviewerId, Long revieweeId);

    List<Review> findByRevieweeId(Long revieweeId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.reviewee.id = :userId")
    Double getAverageRatingForUser(Long userId);
}
