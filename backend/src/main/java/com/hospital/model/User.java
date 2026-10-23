package com.hospital.model;

import java.sql.Timestamp;

/**
 * Base User model class.
 * Satisfies MCA Semester 1 Syllabus Phase 1 (Core Application Setup and Design).
 * Demonstrates: Object-Oriented Inheritance, Encapsulation, Constructors,
 * and String validation methods.
 */
public class User {
    protected int id;
    protected String name;
    protected String email;
    protected String role; // "PATIENT" or "ADMIN"
    protected Timestamp createdAt;

    public User() {
    }

    public User(String name, String email, String role) {
        setName(name);
        setEmail(email);
        this.role = role;
    }

    public User(int id, String name, String email, String role, Timestamp createdAt) {
        this.id = id;
        setName(name);
        setEmail(email);
        this.role = role;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = (name != null) ? name.trim() : null;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = (email != null) ? email.trim().toLowerCase() : null;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Validates if a string attribute contains valid text.
     */
    public static boolean isValidString(String value, int minLength, int maxLength) {
        if (value == null) return false;
        String trimmed = value.trim();
        return trimmed.length() >= minLength && trimmed.length() <= maxLength;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}

