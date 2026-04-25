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

@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Owner
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // Sitter
    @ManyToOne
    @JoinColumn(name = "sitter_id", nullable = false)
    private User sitter;

    // Pet being booked
    @ManyToOne
    @JoinColumn(name = "pet_id")
    private Pet pet;

    // ✅ NEW FIELD (Contact Info)
    @Column(name = "contact_info")
    private String contactInfo;

    private LocalDate startDate;
    private LocalDate endDate;

    private String serviceType;

    private String requestMessage;

    private String status;

    private String declineMessage;

    private LocalDateTime createdAt;

    public Booking() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public User getSitter() {
        return sitter;
    }

    public void setSitter(User sitter) {
        this.sitter = sitter;
    }

    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getRequestMessage() {
        return requestMessage;
    }

    public void setRequestMessage(String requestMessage) {
        this.requestMessage = requestMessage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeclineMessage() {
        return declineMessage;
    }

    public void setDeclineMessage(String declineMessage) {
        this.declineMessage = declineMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
