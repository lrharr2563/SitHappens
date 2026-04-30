package com.sithappens.sithappens.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sithappens.sithappens.model.User;

// handles database operations for User
public interface UserRepository extends JpaRepository<User, Long> {

    // find user by email (used for login)
    User findByEmail(String email);

}
