package com.sithappens.sithappens.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sithappens.sithappens.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

}


