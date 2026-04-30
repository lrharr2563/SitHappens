/*
 * File: Review.java
 *
 * Team Contributions:
 *
 * Lauren Harrington:
 * - Created the review entity and set up how it connects to users and bookings
 * - Handled the backend structure and database relationships for reviews
 *
 * Margaret Jeannotte:
 * - Helped connect review data to the frontend and made sure it displays correctly
 * - Tested how reviews appear across dashboards and pages
 *
 * Vida Familia Piccirillo:
 * - Worked on review logic (only allowing reviews after completed bookings)
 * - Helped implement rating system and how reviews connect to bookings
 * - Tested review functionality to make sure everything works correctly
 */

package com.sithappens.sithappens.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

// represents a review after a completed booking
@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // rating from 1-5 stars
    private int rating;

    // written feedback
    private String comment;

    // who is reviewing who (owner to sitter or sitter to owner)
    private String reviewType; // OWNER_TO_SITTER or SITTER_TO_OWNER

    // user who wrote the review
    @ManyToOne
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    // user being reviewed
    @ManyToOne
    @JoinColumn(name = "reviewee_id")
    private User reviewee;

    // link review to a specific booking
    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    public Review() {
    }

    public Long getId() {
        return id;
    }

    public int getRating() {
        return rating;
    }

    // set rating value
    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    // set review message
    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getReviewType() {
        return reviewType;
    }

    // set direction of review (owner to sitter or sitter to owner)
    public void setReviewType(String reviewType) {
        this.reviewType = reviewType;
    }

    public User getReviewer() {
        return reviewer;
    }

    // who wrote the review
    public void setReviewer(User reviewer) {
        this.reviewer = reviewer;
    }

    public User getReviewee() {
        return reviewee;
    }

    // who the review is about
    public void setReviewee(User reviewee) {
        this.reviewee = reviewee;
    }

    public Booking getBooking() {
        return booking;
    }

    // connect review to booking
    public void setBooking(Booking booking) {
        this.booking = booking;
    }
}