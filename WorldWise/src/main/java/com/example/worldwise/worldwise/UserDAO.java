package com.example.worldwise.worldwise;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class UserDAO {

    private static UserDAO instance;

    private UserDAO() {}

    public static UserDAO getInstance() {
        if (instance == null) {
            instance = new UserDAO();
        }
        return instance;
    }

    // Hash password with SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString(); // Hex string
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password: " + e.getMessage(), e);
        }
    }

    // Register User
    public boolean registerUser(User user) {
        // Validate email before proceeding
        if (!isValidEmail(user.getEmail())) {
            System.out.println("Invalid email: " + user.getEmail());
            return false;
        }

        String sql = "INSERT INTO users (firstname, lastname, email, password) VALUES (?, ?, ?, ?)";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getFirstname());
            pstmt.setString(2, user.getLastname());
            pstmt.setString(3, user.getEmail());

            // Hash before storing
            String hashedPassword = hashPassword(user.getPassword());
            pstmt.setString(4, hashedPassword);

            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Error registering user: " + e.getMessage());
            return false;
        }
    }

    // Email validation helper (same logic as in RegisterController)
    private boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) return false;
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }

    // Validate Login (compare hash)
    public boolean validateLogin(String email, String password) {
        String sql = "SELECT password FROM users WHERE email = ?";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");
                String inputHash = hashPassword(password);
                return storedHash.equals(inputHash);
            }

        } catch (SQLException e) {
            System.out.println("Error validating login: " + e.getMessage());
        }
        return false;
    }
}