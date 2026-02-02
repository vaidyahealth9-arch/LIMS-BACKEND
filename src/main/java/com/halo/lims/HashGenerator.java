package com.halo.lims;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class HashGenerator {
    public static void main(String[] args) {
//        if (args.length == 0)
//        {
//            System.out.println("Please provide a password to hash as a command-line argument.");
//        }
//        System.out.println("Usage: java -cp <classpath> HashGenerator <password>");
        String passwordToHash = "techpassword123";
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedPassword = passwordEncoder.encode(passwordToHash);
        System.out.println("Original Password: " + passwordToHash);
        System.out.println("BCrypt Hashed Value: " + hashedPassword);    }
    // $2a$10$JP3DoBRwEP9FfXd84iRS1e3uB/suq5HYYjG4aABYI9UvCJg8Q5eca
    // $2a$10$dBABNQrHI7Jk4iQuOElDIuGoUt3Dh6B9Zftwc/dFowQgfpP3nIvoG
}
