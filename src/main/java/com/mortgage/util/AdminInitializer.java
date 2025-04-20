package com.mortgage.util;

import com.mortgage.database.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminInitializer {
    public static void initializeAdminUser() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // First check if admin user exists
            String checkQuery = "SELECT username FROM users WHERE username = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setString(1, "admin");
                ResultSet rs = checkStmt.executeQuery();
                
                if (!rs.next()) {
                    // Admin doesn't exist, create it with all required fields
                    String insertQuery = """
                        INSERT INTO users (
                            username, 
                            password_hash, 
                            email,
                            first_name,
                            last_name,
                            phone_number,
                            address,
                            role
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """;
                    
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                        String hashedPassword = BCrypt.hashpw("admin123", BCrypt.gensalt());
                        
                        insertStmt.setString(1, "admin");                    // username
                        insertStmt.setString(2, hashedPassword);             // password_hash
                        insertStmt.setString(3, "admin@mortgage.com");       // email
                        insertStmt.setString(4, "Admin");                    // first_name
                        insertStmt.setString(5, "User");                     // last_name
                        insertStmt.setString(6, "1234567890");              // phone_number
                        insertStmt.setString(7, "Admin Address");           // address
                        insertStmt.setString(8, "ADMIN");                   // role
                        
                        int rowsAffected = insertStmt.executeUpdate();
                        if (rowsAffected > 0) {
                            System.out.println("Admin user created successfully");
                        } else {
                            System.out.println("Failed to create admin user");
                        }
                    }
                } else {
                    System.out.println("Admin user already exists");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in AdminInitializer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean verifyAdminCredentials(String username, String password) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT password_hash FROM users WHERE username = ? AND role = 'ADMIN'";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    return BCrypt.checkpw(password, storedHash);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error verifying admin credentials: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
} 