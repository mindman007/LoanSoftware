package com.mortgage.util;

import com.mortgage.database.DatabaseConnection;

public class DatabaseTest {
    public static void main(String[] args) {
        try {
            // Initialize database and create admin user
            System.out.println("Initializing database...");
            DatabaseConnection.initializeDatabase();
            
            // Test admin login
            boolean adminLoginSuccess = AdminInitializer.verifyAdminCredentials("admin", "admin123");
            System.out.println("Admin login test: " + (adminLoginSuccess ? "SUCCESS" : "FAILED"));
            
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnection.closeConnection();
        }
    }
} 