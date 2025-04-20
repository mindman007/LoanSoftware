CREATE TABLE mortgages (
    id BIGSERIAL PRIMARY KEY,
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
);

-- Create trigger function for updating updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger
CREATE TRIGGER update_mortgages_updated_at
    BEFORE UPDATE ON mortgages
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();