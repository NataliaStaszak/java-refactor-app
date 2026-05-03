package com.sap.refactoring.users;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    void deleteByEmail(String email);

    List<User> findAllByName(String name);

    boolean existsByEmail(String email);
}