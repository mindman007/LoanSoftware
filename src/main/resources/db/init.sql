-- Create database
CREATE DATABASE mortgage_app;

-- Connect to the database
\c mortgage_app;

-- Create users table if not exists
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert admin user if not exists
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin') THEN
        INSERT INTO users (username, password) 
        VALUES ('admin', '$2a$10$xLxZQXZxK8HXZxz8.H0NXeqPYkxVpK1QDcyy6ERJXhQpqxMOPP7Vy');
        -- The password hash above is for 'admin123' using BCrypt
    END IF;
END
$$;

-- Create applications table
CREATE TABLE applications (
    id SERIAL PRIMARY KEY,
    agent_id INTEGER REFERENCES users(id),
    name VARCHAR(100),
    mortgage_type VARCHAR(50),
    actual_weight DECIMAL(12, 2),
    start_date DATE,
    application_date DATE,
    difference_days INTEGER,
    interest_rate DECIMAL(5, 2),
    interest_amount DECIMAL(12, 2),
    unique_number VARCHAR(50) UNIQUE,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
); 