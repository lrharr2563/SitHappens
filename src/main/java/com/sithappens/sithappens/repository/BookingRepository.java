package com.sithappens.sithappens.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sithappens.sithappens.model.Booking;

// handles database operations for Booking
public interface BookingRepository extends JpaRepository<Booking, Long> {
}
