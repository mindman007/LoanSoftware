package com.mortgage.database;

import com.mortgage.util.AdminInitializer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.mindrot.jbcrypt.BCrypt;

public class DatabaseConnection {
    // PostgreSQL database configuration
    public static final String URL = "jdbc:postgresql://localhost:5432/mortgage_app";
    public static final String USER = "postgres";
    public static final String PASSWORD = "9935443133";

    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Register PostgreSQL driver
                Class.forName("org.postgresql.Driver");
                
                // Create connection
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (ClassNotFoundException e) {
                throw new SQLException("PostgreSQL JDBC Driver not found.", e);
            }
        }
        return connection;
    }

    // Utility method to check if a column exists
    private static boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        String query = """
            SELECT EXISTS (
                SELECT 1 
                FROM information_schema.columns 
                WHERE table_name = ? 
                AND column_name = ?
            )
        """;
        
        try (var stmt = conn.prepareStatement(query)) {
            stmt.setString(1, tableName);
            stmt.setString(2, columnName);
            try (var rs = stmt.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        }
    }

    public static void createMortgagesTable() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            String sql = """
                CREATE TABLE IF NOT EXISTS mortgages (
                    id SERIAL PRIMARY KEY,
                    user_id VARCHAR(255) NOT NULL,
                    name VARCHAR(255) NOT NULL,
                    sex VARCHAR(50) NOT NULL,
                    date_of_birth DATE NOT NULL,
                    loan_amount DECIMAL(15,2) NOT NULL,
                    thing_name VARCHAR(255) NOT NULL,
                    interest_rate DECIMAL(5,2) NOT NULL,
                    submission_date DATE NOT NULL,
                    weight DECIMAL(10,2) NOT NULL,
                    gold_rate DECIMAL(10,2),
                    silver_rate DECIMAL(10,2),
                    address TEXT NOT NULL,
                    status VARCHAR(50) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;
            
            stmt.executeUpdate(sql);

            // Add trigger for updated_at
            String triggerSql = """
                CREATE OR REPLACE FUNCTION update_updated_at_column()
                RETURNS TRIGGER AS $$
                BEGIN
                    NEW.updated_at = CURRENT_TIMESTAMP;
                    RETURN NEW;
                END;
                $$ language 'plpgsql';
                
                DROP TRIGGER IF EXISTS update_mortgages_updated_at ON mortgages;
                
                CREATE TRIGGER update_mortgages_updated_at
                    BEFORE UPDATE ON mortgages
                    FOR EACH ROW
                    EXECUTE FUNCTION update_updated_at_column();
                """;
            
            stmt.executeUpdate(triggerSql);
        }
    }

    // Add this to initializeDatabase method
    public static void initializeDatabase() throws SQLException {
        try (Connection conn = getConnection()) {
            // Create users table if not exists
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS users (" +
                    "username VARCHAR(50) PRIMARY KEY," +
                    "password VARCHAR(100)," +
                    "email VARCHAR(100)," +
                    "role VARCHAR(20)" +
                    ")"
                );
            }

            // Check if admin exists
            try (PreparedStatement checkStmt = conn.prepareStatement("SELECT username FROM users WHERE username = ?")) {
                checkStmt.setString(1, "admin");
                ResultSet rs = checkStmt.executeQuery();
                
                // If admin doesn't exist, create it
                if (!rs.next()) {
                    try (PreparedStatement insertStmt = conn.prepareStatement(
                            "INSERT INTO users (username, password, email, role) VALUES (?, ?, ?, ?)")) {
                        String hashedPassword = BCrypt.hashpw("admin123", BCrypt.gensalt());
                        insertStmt.setString(1, "admin");
                        insertStmt.setString(2, hashedPassword);
                        insertStmt.setString(3, "admin@system.com");
                        insertStmt.setString(4, "ADMIN");
                        insertStmt.executeUpdate();
                    }
                }
            }
        }
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
}