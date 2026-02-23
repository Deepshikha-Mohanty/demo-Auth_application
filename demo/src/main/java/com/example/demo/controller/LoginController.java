package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.security.Principal;
import java.util.Optional;

@Controller
public class LoginController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public LoginController(UserService userService,
                           PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }


    // LOGIN PAGE
    @GetMapping("/")
    public String loginPage() {
        return "login"; // login.html
    }


    // REGISTRATION
    @GetMapping("/register")
    public String registerPage() {
        return "registration"; // registration.html
    }

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


    // ADMIN DASHBOARD
    @GetMapping("/admin-dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin-dashboard"; // admin_dashboard.html
    }


    // USER DASHBOARD (LOCAL + OIDC)
    @GetMapping("/user-dashboard")
    public String userDashboard(Model model,
                                Principal principal,
                                @AuthenticationPrincipal OidcUser oidcUser) {

        User user = null;

        // ==========================
        // OIDC LOGIN (Jans / Gluu)
        // ==========================
        if (oidcUser != null) {

            // ----- EMAIL (Mandatory) -----
            String email = Optional.ofNullable(oidcUser.getEmail())
                    .orElse(oidcUser.getAttribute("email"));

            if (email == null) {
                return "redirect:/login";
            }

            // ----- BASIC DETAILS -----
            String firstName = Optional.ofNullable(oidcUser.getGivenName())
                    .orElse(oidcUser.getAttribute("given_name"));

            String lastName = Optional.ofNullable(oidcUser.getFamilyName())
                    .orElse(oidcUser.getAttribute("family_name"));

            String fullName = Optional.ofNullable(oidcUser.getFullName())
                    .orElse((firstName != null ? firstName : "") +
                            (lastName != null ? " " + lastName : ""));

            // ----- PHONE (Handle Jans / Custom LDAP mappings) -----
            String phone = Optional.ofNullable(oidcUser.getPhoneNumber())
                    .orElse(oidcUser.getAttribute("phone_number"));

            if (phone == null) {
                phone = oidcUser.getAttribute("mobile");
            }

            if (phone == null) {
                phone = oidcUser.getAttribute("telephoneNumber");
            }

            // ----- FIND OR CREATE USER -----
            String finalPhone = phone;
            user = userService.findByUsername(email)
                    .map(existingUser -> {

                        // Optional: keep DB synced with SSO
                        existingUser.setFirstName(firstName);
                        existingUser.setLastName(lastName);
                        existingUser.setFullName(fullName);

                        if (finalPhone != null) {
                            existingUser.setMobNo(finalPhone);
                        }

                        return userService.save(existingUser);
                    })
                    .orElseGet(() -> {

                        User newUser = new User();
                        newUser.setUsername(email);
                        newUser.setEmail(email);
                        newUser.setFirstName(firstName);
                        newUser.setLastName(lastName);
                        newUser.setFullName(fullName);
                        newUser.setMobNo(finalPhone); // may be null
                        newUser.setCountryCode("+91");
                        newUser.setRole("Normal User");

                        return userService.save(newUser);
                    });
        }

        // ==========================
        // LOCAL LOGIN
        // ==========================
        else if (principal != null) {

            user = userService.findByUsername(principal.getName())
                    .orElseThrow(() ->
                            new RuntimeException("User not found in database"));
        }

        // ==========================
        // SAFETY CHECK
        // ==========================
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        return "user-dashboard";
    }
    // UPDATE PROFILE (BOTH TYPES)
    @PostMapping("/update-profile")
    public String updateProfile(@ModelAttribute User updatedUser,
                                Principal principal,
                                @AuthenticationPrincipal OidcUser oidcUser) {

        String username = (oidcUser != null) ? oidcUser.getEmail() : principal.getName();

        User existingUser = userService
                .findByUsername(username)
                .orElseThrow();

        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setFullName(updatedUser.getFullName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setCountryCode(updatedUser.getCountryCode());
        existingUser.setMobNo(updatedUser.getMobNo());

        if (updatedUser.getPassword() != null &&
                !updatedUser.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        userService.save(existingUser);

        return "redirect:/user-dashboard";
    }


    // ADMIN DELETE USER
    @PostMapping("/delete-user/{uid}")
    public String deleteUser(@PathVariable int uid) {
        userService.deleteUser(uid);
        return "redirect:/admin-dashboard";
    }


    // ADMIN UPDATE USER
    @PostMapping("/admin-update-user/{uid}")
    public String adminUpdateUser(@PathVariable int uid,
                                  @ModelAttribute User updatedUser) {

        User existingUser = userService.findById(uid).orElseThrow();

        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setFullName(updatedUser.getFullName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setCountryCode(updatedUser.getCountryCode());
        existingUser.setMobNo(updatedUser.getMobNo());
        existingUser.setRole(updatedUser.getRole());

        if (updatedUser.getPassword() != null &&
                !updatedUser.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        userService.save(existingUser);

        return "redirect:/admin-dashboard";
    }



    // ADMIN CREATE NEW USER
    @PostMapping("/admin-register")
    public String registerUserAdmin(@ModelAttribute User user, Model model) {

        if (userService.usernameExists(user.getUsername())) {
            model.addAttribute("error", true);
            return "admin-dashboard"; // reload dashboard with error
        }

        // encode password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.registerUser(user);

        model.addAttribute("success", true);
        return "redirect:/admin-dashboard"; // reload dashboard after success
    }
}