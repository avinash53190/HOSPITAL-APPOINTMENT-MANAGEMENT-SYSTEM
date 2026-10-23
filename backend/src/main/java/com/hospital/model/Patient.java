package com.hospital.model;

import java.sql.Timestamp;

/**
 * Model class representing a Patient.
 * Extends User to demonstrate:
 * - OOP Inheritance (Patient is-a User)
 * - Encapsulation (private fields with accessor methods)
 * - Constructor chaining via super()
 * - Method overriding (toString)
 */
public class Patient extends User {
    private String phone;
    private String passwordHash;

    // Default constructor
    public Patient() {
        super();
        setRole("PATIENT");
    }

    // Parameterized constructor for registration
    public Patient(String name, String email, String phone, String passwordHash) {
        super(name, email, "PATIENT");
        this.phone = phone;
        this.passwordHash = passwordHash;
    }

    // Full constructor
    public Patient(int patientId, String name, String email, String phone, String passwordHash, Timestamp createdAt) {
        super(patientId, name, email, "PATIENT", createdAt);
        this.phone = phone;
        this.passwordHash = passwordHash;
    }

    // Backwards-compatible aliases for patientId
    public int getPatientId() {
        return getId();
    }

    public void setPatientId(int patientId) {
        setId(patientId);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = (phone != null) ? phone.trim() : null;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Override
    public String toString() {
        return "Patient{" +
                "patientId=" + getId() +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", phone='" + phone + '\'' +
                ", role='" + getRole() + '\'' +
                '}';
    }
}
