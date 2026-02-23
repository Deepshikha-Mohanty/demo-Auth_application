package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepo userRepo;

    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    // REGISTER LOCAL USER
    public void registerUser(User user) {
        userRepo.save(user);
    }


    // FIND BY USERNAME (Local Login)
    public Optional<User> findByUsername(String username) {
        return userRepo.findByUsername(username);
    }


    // FIND BY EMAIL (OIDC Login)
    public Optional<User> findByEmail(String email) {
        return userRepo.findByEmail(email);
    }


    // CHECK USERNAME EXISTS
    public boolean usernameExists(String username) {
        return userRepo.findByUsername(username).isPresent();
    }


    // AUTO CREATE OIDC USER
    public User createOidcUser(String email, String fullName) {

        User user = new User();
        user.setUsername(email);       // using email as username
        user.setEmail(email);
        user.setFullName(fullName);
        user.setRole("Normal User");

        return userRepo.save(user);
    }


    // ADMIN DASHBOARD
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }


    // SAVE / UPDATE
    public User save(User user) {
        return userRepo.save(user);
    }


    // DELETE USER
    public void deleteUser(int uid) {
        userRepo.deleteById(uid);
    }

    public Optional<User> findById(int uid) {
        return userRepo.findById(uid);
    }
}