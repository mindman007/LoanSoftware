package com.mortgage.service;

import com.mortgage.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MortgageService {

    public boolean updateMortgageEntry(int mortgageId, String newDetails) {
        String sql = "UPDATE mortgages SET details = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newDetails);
            pstmt.setInt(2, mortgageId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}