package com.sithappens.sithappens.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sithappens.sithappens.model.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
