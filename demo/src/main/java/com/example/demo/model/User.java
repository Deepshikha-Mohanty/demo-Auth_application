package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int uid;

    private String firstName;
    private String lastName;
    private String fullName;

    private String mobNo;
    private String countryCode;

    @Column(unique = true, nullable = false)
    private String username;   // used for local login

    @Column(unique = true)
    private String email;      // used for OIDC login

    private String password;

    private String role;       // Admin / Normal User
}