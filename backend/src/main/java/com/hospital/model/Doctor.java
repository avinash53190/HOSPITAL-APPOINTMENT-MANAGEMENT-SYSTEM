package com.hospital.model;

/**
 * Model class representing a Doctor.
 */
public class Doctor {
    private int doctorId;
    private String name;
    private String specialization;
    private boolean available;

    public Doctor() {
    }

    public Doctor(String name, String specialization, boolean available) {
        this.name = name;
        this.specialization = specialization;
        this.available = available;
    }

    public Doctor(int doctorId, String name, String specialization, boolean available) {
        this.doctorId = doctorId;
        this.name = name;
        this.specialization = specialization;
        this.available = available;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "doctorId=" + doctorId +
                ", name='" + name + '\'' +
                ", specialization='" + specialization + '\'' +
                ", available=" + available +
                '}';
    }
}

