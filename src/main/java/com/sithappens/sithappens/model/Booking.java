/*
 * Team Contributions:
 *
 * Lauren Harrington:
 * - Built the booking entity and set up relationships between users, pets, and bookings
 * - Handled the backend structure and database connections for bookings
 *
 * Margaret Jeannotte:
 * - Worked on connecting the booking forms from the frontend to this model
 * - Helped make sure booking data shows correctly on the dashboards
 *
 * Vida Familia Piccirillo:
 * - Worked on booking status logic (REQUESTED, CONFIRMED, COMPLETED)
 * - Helped connect bookings to the review system and tested functionality
 */

package com.sithappens.sithappens.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

// represents a booking between an owner and a sitter
@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // the owner who is requesting the booking
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // the sitter being booked
    @ManyToOne
    @JoinColumn(name = "sitter_id", nullable = false)
    private User sitter;

    // pet included in the booking
    @ManyToOne
    @JoinColumn(name = "pet_id")
    private Pet pet;

    // contact info in case sitter needs to reach owner
    @Column(name = "contact_info")
    private String contactInfo;

    // dates for the booking
    private LocalDate startDate;
    private LocalDate endDate;

    // type of service (overnight, walking, etc.)
    private String serviceType;

    // message the owner writes when requesting
    private String requestMessage;

    // REQUESTED, CONFIRMED, COMPLETED, etc.
    private String status;

    // optional message if sitter declines
    private String declineMessage;

    // when the booking was created
    private LocalDateTime createdAt;

    public Booking() {
        // automatically set when booking is made
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getOwner() {
        return owner;
    }

    // link booking to an owner
    public void setOwner(User owner) {
        this.owner = owner;
    }

    public User getSitter() {
        return sitter;
    }

    // link booking to a sitter
    public void setSitter(User sitter) {
        this.sitter = sitter;
    }

    public Pet getPet() {
        return pet;
    }

    // set which pet this booking is for
    public void setPet(Pet pet) {
        this.pet = pet;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    // save owner's contact info
    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    // set booking start date
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    // set booking end date
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getServiceType() {
        return serviceType;
    }

    // set type of service requested
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getRequestMessage() {
        return requestMessage;
    }

    // message from owner to sitter
    public void setRequestMessage(String requestMessage) {
        this.requestMessage = requestMessage;
    }

    public String getStatus() {
        return status;
    }

    // update booking status
    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeclineMessage() {
        return declineMessage;
    }

    // message if sitter declines request
    public void setDeclineMessage(String declineMessage) {
        this.declineMessage = declineMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
