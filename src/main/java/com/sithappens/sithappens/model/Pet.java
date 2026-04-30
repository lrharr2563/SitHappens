/*
 * File: Pet.java
 *
 * Team Contributions:
 *
 * Lauren Harrington:
 * - Created the pet entity and set up how it connects to the user (owner)
 * - Handled the backend structure and database mapping for pets
 *
 * Margaret Jeannotte:
 * - Worked on the frontend pet form and connecting it to this model
 * - Helped make sure pet data shows correctly on the dashboards
 *
 * Vida Familia Piccirillo:
 * - Helped expand pet fields (age, breed, notes) and made sure they save correctly
 * - Tested pet feature to make sure it works with the rest of the system
 */

package com.sithappens.sithappens.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// represents a pet that belongs to an owner
@Entity
@Table(name = "pets")
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // basic pet info
    private String name;
    private String type;
    private String breed;
    private Integer age;

    // any extra notes about the pet (feeding, behavior, etc.)
    private String notes;

    // many pets can belong to one owner
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    // path to uploaded pet image
    private String imagePath;

    public String getImagePath() {
        return imagePath;
    }

    // save image location so it can be displayed later
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // set pet name
    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    // set type (dog, cat, etc.)
    public void setType(String type) {
        this.type = type;
    }

    public String getBreed() {
        return breed;
    }

    // set breed if provided
    public void setBreed(String breed) {
        this.breed = breed;
    }

    public Integer getAge() {
        return age;
    }

    // set pet age
    public void setAge(Integer age) {
        this.age = age;
    }

    public User getOwner() {
        return owner;
    }

    public String getNotes() {
        return notes;
    }

    // set any care notes for the sitter
    public void setNotes(String notes) {
        this.notes = notes;
    }

    // link pet to its owner
    public void setOwner(User owner) {
        this.owner = owner;
    }
}
