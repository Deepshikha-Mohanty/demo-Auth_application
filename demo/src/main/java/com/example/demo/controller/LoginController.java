package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
public class LoginController {

    @Autowired
    private final UserService userService;
    @Autowired
    private final PasswordEncoder passwordEncoder;

    public LoginController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    // Login Page
    @GetMapping("/")
    public String loginPage() {
        return "login";
    }

    // Registration Page
    @GetMapping("/register")
    public String registerPage() {
        return "registration";
    }

    // Handle Registration
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {

        if (userService.usernameExists(user.getUsername())) {
            model.addAttribute("error", true);
            return "login";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.registerUser(user);
        model.addAttribute("success", true);
        return "login";
    }

    // Admin Dashboard
    @GetMapping("/admin-dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin dashboard";
    }

    // User Dashboard
    @GetMapping("/user-dashboard")
    public String userDashboard(Model model, Principal principal) {

        User user = userService
                .findByUsername(principal.getName())
                .orElseThrow();

        model.addAttribute("user", user);

        return "user dashboard";
    }

    @PostMapping("/update-profile")
    public String updateProfile(@ModelAttribute User updatedUser,
                                Principal principal) {

        User existingUser = userService
                .findByUsername(principal.getName())
                .orElseThrow();

        // Update only editable fields
        existingUser.setFirst_Name(updatedUser.getFirst_Name());
        existingUser.setLast_Name(updatedUser.getLast_Name());
        existingUser.setFull_Name(updatedUser.getFull_Name());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setCountryCode(updatedUser.getCountryCode());
        existingUser.setMob_No(updatedUser.getMob_No());
        if (updatedUser.getPassword() != null &&
                !updatedUser.getPassword().isEmpty()) {

            existingUser.setPassword(
                    passwordEncoder.encode(updatedUser.getPassword())
            );
        }
        existingUser.setUsername(updatedUser.getUsername());

        userService.save(existingUser);

        return "user dashboard";
    }
    @PostMapping("/delete-user/{uid}")
    public String deleteUser(@PathVariable int uid) {

        userService.deleteUser(uid);

        return "redirect:/admin-dashboard";
    }


    @PostMapping("/admin-update-user/{uid}")
    public String adminUpdateUser(@PathVariable int uid,
                                  @ModelAttribute User updatedUser) {

        User existingUser = userService.findById(uid).orElseThrow();

        existingUser.setFirst_Name(updatedUser.getFirst_Name());
        existingUser.setLast_Name(updatedUser.getLast_Name());
        existingUser.setFull_Name(updatedUser.getFull_Name());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setCountryCode(updatedUser.getCountryCode());
        existingUser.setMob_No(updatedUser.getMob_No());
        existingUser.setRole(updatedUser.getRole());
        if (updatedUser.getPassword() != null &&
                !updatedUser.getPassword().isEmpty()) {

            existingUser.setPassword(
                    passwordEncoder.encode(updatedUser.getPassword())
            );
        }

        userService.save(existingUser);

        return "redirect:/admin-dashboard";
    }

    @PostMapping("/admin-register")
    public String registerUserAdmin(@ModelAttribute User user, Model model) {

        if (userService.usernameExists(user.getUsername())) {
            model.addAttribute("error", true);
            return "login";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.registerUser(user);
        model.addAttribute("success", true);
        return "redirect:/admin-dashboard";
    }
}