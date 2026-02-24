package com.sithappens.sithappens.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate availableDate;

    @ManyToOne
    @JoinColumn(name = "sitter_id")
    private User sitter;

    public Availability() {}

    public Availability(LocalDate availableDate, User sitter) {
        this.availableDate = availableDate;
        this.sitter = sitter;
    }

    public Long getId() { return id; }

    public LocalDate getAvailableDate() { return availableDate; }
    public void setAvailableDate(LocalDate availableDate) {
        this.availableDate = availableDate;
    }

    public User getSitter() { return sitter; }
    public void setSitter(User sitter) {
        this.sitter = sitter;
    }
}