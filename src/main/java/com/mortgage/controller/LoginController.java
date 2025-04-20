package com.mortgage.controller;

import javax.swing.*;
import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;
import com.mortgage.database.DatabaseConnection;

public class LoginController {
    private final LoginCallback callback;

    public LoginController(LoginCallback callback) {
        this.callback = callback;
    }

    public void handleLogin(String username, String password, String userType) {
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter both username and password");
            return;
        }

        // Handle default admin login
        if ("Admin".equals(userType) && "admin".equals(username) && "admin123".equals(password)) {
            callback.onLoginSuccess(username);
            return;
        }

        // For regular users, check database
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "SELECT password, role FROM users WHERE username = ?")) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                String userRole = rs.getString("role");
                
                if (hashedPassword == null) {
                    JOptionPane.showMessageDialog(null, "Invalid user account configuration");
                    return;
                }

                // Verify user type matches
                if (!userRole.equalsIgnoreCase(userType)) {
                    JOptionPane.showMessageDialog(null, "Invalid login type for this user");
                    return;
                }
                
                if (BCrypt.checkpw(password, hashedPassword)) {
                    callback.onLoginSuccess(username);
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid password");
                }
            } else {
                JOptionPane.showMessageDialog(null, "User not found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Login error: " + e.getMessage());
        }
    }

    public void registerUser(String username, String password, String email) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if username already exists
            try (PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT username FROM users WHERE username = ?")) {
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    JOptionPane.showMessageDialog(null, "Username already exists");
                    return;
                }
            }
            
            // Insert new user
            try (PreparedStatement insertStmt = conn.prepareStatement(
                    "INSERT INTO users (username, password, email, role) VALUES (?, ?, ?, ?)")) {
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                insertStmt.setString(1, username);
                insertStmt.setString(2, hashedPassword);
                insertStmt.setString(3, email);
                insertStmt.setString(4, "User");
                insertStmt.executeUpdate();
                JOptionPane.showMessageDialog(null, "Registration successful! You can now login.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Registration error: " + e.getMessage());
        }
    }

    public interface LoginCallback {
        void onLoginSuccess(String username);
    }
}