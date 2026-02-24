package com.sithappens.sithappens.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sithappens.sithappens.model.Pet;

public interface PetRepository extends JpaRepository<Pet, Long> {
}

