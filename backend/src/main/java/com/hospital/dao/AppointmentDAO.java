package com.hospital.dao;

import com.hospital.model.Appointment;
import com.hospital.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Appointment operations.
 * Demonstrates JDBC prepared statements, transaction management, connection handling,
 * and parameterized SQL queries to prevent SQL Injection.
 */
public class AppointmentDAO {

    /**
     * Checks if a doctor already has an active (CONFIRMED) booking at the given date and time.
     */
    public boolean isSlotTaken(int doctorId, Date date, String time) {
        String sql = "SELECT COUNT(*) FROM appointments WHERE doctor_id = ? AND appointment_date = ? AND appointment_time = ? AND status = 'CONFIRMED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ps.setDate(2, date);
            ps.setString(3, time.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Books a new appointment with transaction management.
     * Checks for conflicting bookings atomically within a transaction to prevent race conditions.
     */
    public synchronized boolean bookAppointment(Appointment appointment) {
        String checkSql = "SELECT COUNT(*) FROM appointments WHERE doctor_id = ? AND appointment_date = ? AND appointment_time = ? AND status = 'CONFIRMED'";
        String insertSql = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, status) VALUES (?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            // Step 1: Check slot within transaction
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setInt(1, appointment.getDoctorId());
                checkPs.setDate(2, appointment.getAppointmentDate());
                checkPs.setString(3, appointment.getAppointmentTime().trim());
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        conn.rollback(); // Conflict detected; abort
                        return false;
                    }
                }
            }

            // Step 2: Insert confirmed booking
            try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                insertPs.setInt(1, appointment.getPatientId());
                insertPs.setInt(2, appointment.getDoctorId());
                insertPs.setDate(3, appointment.getAppointmentDate());
                insertPs.setString(4, appointment.getAppointmentTime().trim());
                insertPs.setString(5, "CONFIRMED");
                int affected = insertPs.executeUpdate();

                if (affected > 0) {
                    conn.commit(); // Commit transaction successfully
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Retrieves all appointments for a specific logged-in patient.
     * Joins with doctors table to display doctor name and specialization.
     */
    public List<Appointment> getAppointmentsByPatient(int patientId) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.appointment_id, a.patient_id, a.doctor_id, a.appointment_date, " +
                     "       a.appointment_time, a.status, a.created_at, " +
                     "       d.name AS doctor_name, d.specialization AS doctor_specialization " +
                     "FROM appointments a " +
                     "JOIN doctors d ON a.doctor_id = d.doctor_id " +
                     "WHERE a.patient_id = ? " +
                     "ORDER BY a.appointment_date DESC, a.appointment_time DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Appointment appt = new Appointment(
                            rs.getInt("appointment_id"),
                            rs.getInt("patient_id"),
                            rs.getInt("doctor_id"),
                            rs.getDate("appointment_date"),
                            rs.getString("appointment_time"),
                            rs.getString("status"),
                            rs.getTimestamp("created_at")
                    );
                    appt.setDoctorName(rs.getString("doctor_name"));
                    appt.setDoctorSpecialization(rs.getString("doctor_specialization"));
                    list.add(appt);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Retrieves all appointments in the system for Admin view.
     * Joins with both doctors and patients tables.
     */
    public List<Appointment> getAllAppointments() {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.appointment_id, a.patient_id, a.doctor_id, a.appointment_date, " +
                     "       a.appointment_time, a.status, a.created_at, " +
                     "       d.name AS doctor_name, d.specialization AS doctor_specialization, " +
                     "       p.name AS patient_name, p.phone AS patient_phone " +
                     "FROM appointments a " +
                     "JOIN doctors d ON a.doctor_id = d.doctor_id " +
                     "JOIN patients p ON a.patient_id = p.patient_id " +
                     "ORDER BY a.appointment_date DESC, a.appointment_time DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Appointment appt = new Appointment(
                        rs.getInt("appointment_id"),
                        rs.getInt("patient_id"),
                        rs.getInt("doctor_id"),
                        rs.getDate("appointment_date"),
                        rs.getString("appointment_time"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at")
                );
                appt.setDoctorName(rs.getString("doctor_name"));
                appt.setDoctorSpecialization(rs.getString("doctor_specialization"));
                appt.setPatientName(rs.getString("patient_name"));
                appt.setPatientPhone(rs.getString("patient_phone"));
                list.add(appt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Allows a patient to cancel their own appointment.
     * Uses both appointment_id AND patient_id to enforce authorization at the database level!
     */
    public boolean cancelAppointmentByPatient(int appointmentId, int patientId) {
        String sql = "UPDATE appointments SET status = 'CANCELLED' WHERE appointment_id = ? AND patient_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            ps.setInt(2, patientId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Returns the total appointment count for a given patient.
     */
    public int getAppointmentCountByPatient(int patientId) {
        String sql = "SELECT COUNT(*) FROM appointments WHERE patient_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
