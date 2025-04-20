package com.mortgage.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Update these values according to your PostgreSQL installation
    private static final String URL = "jdbc:postgresql://localhost:5432/mortgage_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "9935443133"; // Change this to your PostgreSQL password

    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }

    public static void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
} 