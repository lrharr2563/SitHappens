package com.sithappens.sithappens.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sithappens.sithappens.model.Availability;

// handles database operations for Availability
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
}
