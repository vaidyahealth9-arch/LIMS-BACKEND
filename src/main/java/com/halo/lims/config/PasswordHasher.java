package com.halo.lims.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHasher {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode("adminpass");
        System.out.println("Hashed password for 'adminpass': " + hashedPassword);
    }
}
