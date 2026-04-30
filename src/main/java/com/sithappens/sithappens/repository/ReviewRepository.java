/*
 * File: ReviewRepository.java
 *
 * Team Contributions:
 *
 * Lauren Harrington:
 * - Set up repository structure and handled database queries for reviews
 * - Connected review data to the backend using JPA
 *
 * Margaret Jeannotte:
 * - Worked on displaying review data on the frontend
 * - Helped make sure review information shows correctly for users
 *
 * Vida Familia Piccirillo:
 * - Worked on review-related logic and how reviews connect to bookings
 * - Helped test queries to make sure review data is accurate
 */

package com.sithappens.sithappens.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sithappens.sithappens.model.Review;

// handles database operations for Review
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // find a specific review tied to a booking and users (prevents duplicates)
    Review findByBookingIdAndReviewerIdAndRevieweeId(Long bookingId, Long reviewerId, Long revieweeId);

    // get all reviews for a specific user
    List<Review> findByRevieweeId(Long revieweeId);

    // calculate average rating for a user
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.reviewee.id = :userId")
    Double getAverageRatingForUser(Long userId);
}
