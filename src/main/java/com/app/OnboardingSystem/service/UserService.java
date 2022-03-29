package com.app.OnboardingSystem.service;

import com.app.OnboardingSystem.model.User;
import com.app.OnboardingSystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    /**
     * Save user.
     *
     * @param user the user
     */
    public ResponseEntity<Object> createUser(User user) {
        userRepository.save(user);
        return new ResponseEntity<>("User is successfully saved", HttpStatus.OK);
    }

    public void update(User user) {
        userRepository.save(user);
    }

/*    public boolean loginWithEmailPassword(String email, String password) {
        Optional<User> user = userRepository.findUserByEmailAndPassword(email, password);
        if (user.isPresent()) {
            return true;
        } else {
            return false;
        }
    }*/

    public User getUserById(Integer userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            return user.get();
        } else {
            return null;
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findUserByPhoneNumber(username);
        if (user.isPresent()) {
            return new org.springframework.security.core.userdetails.User(user.get().getPhoneNumber(), "$2a$12$WnRkUuCKj.a3BkbTvnnGyuEWWqYc7iD/up7Q9reh7wU6dG5qVaRde", new ArrayList<>());
        } else {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }
}
