package com.sap.refactoring.users;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;



public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findAllByName(String name);

    boolean existsByEmail(String email);
}