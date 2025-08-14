-- Database setup for Pharmacy Management System
CREATE DATABASE IF NOT EXISTS test;
USE test;

-- Create the medicine table
CREATE TABLE IF NOT EXISTS testMed (
    id INT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    formula VARCHAR(255) NOT NULL,
    expiry_date DATE,
    quantity INT NOT NULL,
    price DOUBLE NOT NULL
);

-- Insert some sample data for testing
INSERT INTO testMed (id, name, formula, expiry_date, quantity, price) VALUES
(1, 'Paracetamol', 'C8H9NO2', '2025-12-31', 100, 5.50),
(2, 'Aspirin', 'C9H8O4', '2024-06-30', 5, 8.25),
(3, 'Ibuprofen', 'C13H18O2', '2026-03-15', 50, 12.75),
(4, 'Amoxicillin', 'C16H19N3O5S', '2023-12-31', 25, 15.00),
(5, 'Vitamin C', 'C6H8O6', '2025-09-30', 80, 3.25);

-- Show the created data
SELECT * FROM testMed;
