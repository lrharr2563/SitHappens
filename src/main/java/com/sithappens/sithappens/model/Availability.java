/*
 * File: Availability.java
 *
 * Team Contributions:
 *
 * Lauren Harrington:
 * - Created core backend entity structure and database relationships
 * - Implemented JPA annotations and connected entities to the database
 *
 * Vida Familia Piccirillo:
 * - Developed availability feature logic and connected it to booking system
 * - Ensured availability data works correctly with sitter accounts
 * - Tested availability functionality and database interactions
 *
 * Margaret Jeannotte:
 * - No direct contributions to this file (focused on frontend and UI features)
 */

package com.sithappens.sithappens.model;

import jakarta.persistence.*;
import java.time.LocalDate;

// represents a day a sitter is available
@Entity
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // the actual date the sitter is available
    private LocalDate availableDate;

    // many availability dates can belong to one sitter
    @ManyToOne
    @JoinColumn(name = "sitter_id")
    private User sitter;

    public Availability() {}

    // constructor to quickly create an availability entry
    public Availability(LocalDate availableDate, User sitter) {
        this.availableDate = availableDate;
        this.sitter = sitter;
    }

    public Long getId() { return id; }

    public LocalDate getAvailableDate() { return availableDate; }
    // update the date if needed
    public void setAvailableDate(LocalDate availableDate) {
        this.availableDate = availableDate;
    }

    public User getSitter() { return sitter; }
    // link this availibilty to a sitter
    public void setSitter(User sitter) {
        this.sitter = sitter;
    }
}