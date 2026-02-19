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

    // Register new user (plain password)
    public void registerUser(User user) {
        userRepo.save(user);
    }

    // Find user by username
    public Optional<User> findByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    // Check if username already exists
    public boolean usernameExists(String username) {
        return userRepo.findByUsername(username).isPresent();
    }

    // Get all users (for Admin Dashboard)
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    // Save / Update user (for profile update)
    public User save(User user) {
        return userRepo.save(user);
    }

    // Delete user (optional – for future admin feature)
    public void deleteUser(int uid) {
        userRepo.deleteById(uid);
    }

    public Optional<User> findById(int uid) {
        return userRepo.findById(uid);
    }


}