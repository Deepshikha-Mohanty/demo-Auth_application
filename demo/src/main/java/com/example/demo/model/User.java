package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    int uid;
    String First_Name;
    String Last_Name;
    String Full_Name;
    String Mob_No;
    String username;
    String countryCode;
    String Email;
    String Password;
    String Role;
}
