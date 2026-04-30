/*
 * File: User.java
 *
 * Team Contributions:
 *
 * Lauren Harrington:
 * - Built the user entity and handled how users are stored in the database
 * - Set up relationships between users and other parts of the system
 * - Implemented password hashing and role-based functionality
 *
 * Margaret Jeannotte:
 * - Worked on connecting user data to the frontend (registration and dashboards)
 * - Helped ensure user information displays correctly across pages
 *
 * Vida Familia Piccirillo:
 * - Worked on user-related logic tied to roles (owner vs sitter behavior)
 * - Helped test how users interact with bookings and reviews
 */

package com.sithappens.sithappens.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

// represents a user in the system (owner or sitter)
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // basic user info
    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    // stored as hashed password (not plain text)
    @Column(nullable = false)
    private String passwordHash;

    // determines if user is OWNER or SITTER
    @Column(nullable = false)
    private String role;

    // optional phone number
    private String phone;

    // used for soft delete (instead of actually removing user)
    private boolean active = true;

    // one user can have multiple pets
    @OneToMany(mappedBy = "owner")
    @JsonIgnore
    private List<Pet> pets;

    // Default constructor (required by JPA)
    public User() {}

    // constructor for creating a new user
    public User(String firstName, String lastName, String email, String passwordHash, String role, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.phone = phone;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    // set first name
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    // set last name
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    // set email (must be unique)
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    // store hashed password
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    // set role (OWNER or SITTER)
    public void setRole(String role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    // set phone number
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<Pet> getPets() {
        return pets;
    }

    // link pets to this user
    public void setPets(List<Pet> pets) {
        this.pets = pets;
    }

    public boolean isActive() {
        return active;
    }

    // used to deactivate account instead of deleting
    public void setActive(boolean active) {
        this.active = active;
    }
}


