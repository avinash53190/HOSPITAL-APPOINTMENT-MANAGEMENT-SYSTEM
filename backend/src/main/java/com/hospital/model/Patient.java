package com.hospital.model;

import java.sql.Timestamp;

/**
 * Model class representing a Patient.
 * Demonstrates: Private encapsulation, parameterized constructors,
 * getters and setters, and toString() method.
 */
public class Patient {
    private int patientId;
    private String name;
    private String email;
    private String phone;
    private String passwordHash;
    private Timestamp createdAt;

    // Default constructor
    public Patient() {
    }

    // Parameterized constructor for registration
    public Patient(String name, String email, String phone, String passwordHash) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
    }

    // Full constructor
    public Patient(int patientId, String name, String email, String phone, String passwordHash, Timestamp createdAt) {
        this.patientId = patientId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Patient{" +
                "patientId=" + patientId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}

