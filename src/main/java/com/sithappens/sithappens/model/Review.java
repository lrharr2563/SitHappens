package com.sithappens.sithappens.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int rating; // 1-5 stars

    private String comment;

    // Owner who wrote the review
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    // Sitter being reviewed
    @ManyToOne
    @JoinColumn(name = "sitter_id")
    private User sitter;

    // Booking associated with review
    @OneToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    public Review() {}

    public Review(int rating, String comment, User owner, User sitter, Booking booking) {
        this.rating = rating;
        this.comment = comment;
        this.owner = owner;
        this.sitter = sitter;
        this.booking = booking;
    }

    // Getters and Setters
    public Long getId() { return id; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public User getSitter() { return sitter; }
    public void setSitter(User sitter) { this.sitter = sitter; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
}