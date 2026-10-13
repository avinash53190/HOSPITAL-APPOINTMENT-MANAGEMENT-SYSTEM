-- ========================================================
-- HOSPITAL APPOINTMENT MANAGEMENT SYSTEM
-- Database Schema & Sample Data
-- Database Name: hospital_db
-- ========================================================

CREATE DATABASE IF NOT EXISTS hospital_db;
USE hospital_db;

-- 1. Patients Table
-- Stores registered patient accounts with hashed passwords
CREATE TABLE IF NOT EXISTS patients (
    patient_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Doctors Table
-- Stores doctor profiles and availability status
CREATE TABLE IF NOT EXISTS doctors (
    doctor_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    available BOOLEAN NOT NULL DEFAULT TRUE
);

-- 3. Appointments Table
-- Stores appointment bookings linked to patient and doctor via foreign keys
CREATE TABLE IF NOT EXISTS appointments (
    appointment_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_appointment_patient FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
    CONSTRAINT fk_appointment_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE CASCADE
);

-- Sample Doctors for immediate testing
INSERT INTO doctors (name, specialization, available) VALUES
('Dr. Rajesh Sharma', 'Cardiology', TRUE),
('Dr. Priya Verma', 'Dermatology', TRUE),
('Dr. Amit Patel', 'Pediatrics', TRUE),
('Dr. Sneha Kulkarni', 'Orthopedics', TRUE),
('Dr. Vikram Rao', 'General Medicine', TRUE),
('Dr. Ananya Sen', 'Neurology', TRUE);

