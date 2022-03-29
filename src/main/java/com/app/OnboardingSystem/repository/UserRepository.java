package com.app.OnboardingSystem.repository;

import com.app.OnboardingSystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findUserByPhoneNumber(String phoneNumber);

    Optional<User> findUserByEmail(String email);
    Optional<User> findById(Integer userId);

//    Optional<User> findUserByEmailAndPassword(String email, String password);
}
