package com.mortgage.service;

import com.mortgage.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public class LicenseService {

    public static boolean addLicenseEntry(String licenseKey, LocalDate startDate, LocalDate expiryDate, boolean isActive, String organizationName) {
        String sql = "INSERT INTO licenses (license_key, start_date, expiry_date, is_active, organization_name) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, licenseKey);
            pstmt.setDate(2, java.sql.Date.valueOf(startDate));
            pstmt.setDate(3, java.sql.Date.valueOf(expiryDate));
            pstmt.setBoolean(4, isActive);
            pstmt.setString(5, organizationName);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void createLicenseTable() {
        String sql = "CREATE TABLE IF NOT EXISTS licenses (" +
                     "license_key VARCHAR(255) PRIMARY KEY, " +
                     "start_date DATE NOT NULL, " +
                     "expiry_date DATE NOT NULL, " +
                     "is_active BOOLEAN NOT NULL, " +
                     "organization_name VARCHAR(255) NOT NULL" +
                     ")";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate(sql);
            System.out.println("License table created successfully.");
            
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to create license table.");
        }
    }

    public static boolean hasValidLicense() {
        String sql = "SELECT expiry_date FROM licenses WHERE expiry_date > ? AND is_active = true";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
            ResultSet rs = pstmt.executeQuery();
            
            return rs.next(); // Returns true if there is at least one valid license
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}