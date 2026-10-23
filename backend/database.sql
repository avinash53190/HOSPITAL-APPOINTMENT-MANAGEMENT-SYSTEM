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
    available BOOLEAN NOT NULL DEFAULT TRUE,
    profile_image VARCHAR(500)
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

-- 4. Admins Table
-- Stores administrator credentials with BCrypt password hash
CREATE TABLE IF NOT EXISTS admins (
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seed Default Admin: username 'admin', password 'admin123' (BCrypt hashed)
INSERT INTO admins (username, password_hash)
SELECT 'admin', '$2a$10$w0u3V.Xn3F7E50y0.qFk7eYgTjZ/YkL6rN818bI7tN.o6lKjX21e'
WHERE NOT EXISTS (SELECT 1 FROM admins WHERE username = 'admin');

-- Sample Doctors for immediate testing
INSERT INTO doctors (name, specialization, available)
SELECT new_name, new_specialization, TRUE
FROM (
    SELECT 'Dr. Rajesh Sharma' AS new_name, 'Cardiology' AS new_specialization UNION ALL
    SELECT 'Dr. Priya Verma', 'Dermatology' UNION ALL
    SELECT 'Dr. Amit Patel', 'Pediatrics' UNION ALL
    SELECT 'Dr. Sneha Kulkarni', 'Orthopedics' UNION ALL
    SELECT 'Dr. Vikram Rao', 'General Medicine' UNION ALL
    SELECT 'Dr. Ananya Sen', 'Neurology'
) AS seed_doctors
WHERE NOT EXISTS (
    SELECT 1 FROM doctors existing WHERE existing.name = seed_doctors.new_name
);

UPDATE doctors SET profile_image = CASE name
    WHEN 'Dr. Rajesh Sharma' THEN 'https://i.pravatar.cc/160?img=12'
    WHEN 'Dr. Priya Verma' THEN 'https://i.pravatar.cc/160?img=47'
    WHEN 'Dr. Amit Patel' THEN 'https://i.pravatar.cc/160?img=11'
    WHEN 'Dr. Sneha Kulkarni' THEN 'https://i.pravatar.cc/160?img=32'
    WHEN 'Dr. Vikram Rao' THEN 'https://i.pravatar.cc/160?img=51'
    WHEN 'Dr. Ananya Sen' THEN 'https://i.pravatar.cc/160?img=44'
    ELSE profile_image END;

INSERT INTO doctors (name, specialization, available, profile_image)
SELECT new_name, new_specialization, new_available, new_profile_image
FROM (
    SELECT 'Dr. Meera Iyer' AS new_name, 'Obstetrics & Gynecology' AS new_specialization, TRUE AS new_available, 'https://i.pravatar.cc/160?img=45' AS new_profile_image UNION ALL
    SELECT 'Dr. Arjun Malhotra', 'Gastroenterology', TRUE, 'https://i.pravatar.cc/160?img=13' UNION ALL
    SELECT 'Dr. Kavya Nair', 'Ophthalmology', TRUE, 'https://i.pravatar.cc/160?img=49' UNION ALL
    SELECT 'Dr. Rohan Mehta', 'Psychiatry', TRUE, 'https://i.pravatar.cc/160?img=68' UNION ALL
    SELECT 'Dr. Neha Kapoor', 'Pulmonology', TRUE, 'https://i.pravatar.cc/160?img=25' UNION ALL
    SELECT 'Dr. Sameer Joshi', 'Oncology', TRUE, 'https://i.pravatar.cc/160?img=59'
) AS new_doctors
WHERE NOT EXISTS (
    SELECT 1 FROM doctors existing WHERE existing.name = new_doctors.new_name
);
