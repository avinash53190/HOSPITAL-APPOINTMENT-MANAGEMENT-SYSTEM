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
 * Demonstrates:
 * - Database-level concurrency control via ACID Transactions and pessimistic locking (SELECT ... FOR UPDATE).
 * - Multi-process race condition prevention (independent of JVM synchronization).
 * - Authorization enforcement at the query level (ownership verification).
 * - Parameterized queries preventing SQL Injection.
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
            System.err.println("[AppointmentDAO] Slot check failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Books a new appointment with strict database-level concurrency control.
     * Uses ACID transaction with pessimistic row-locking on the target doctor (SELECT ... FOR UPDATE)
     * to ensure that concurrent booking attempts across threads or multiple server instances
     * cannot reserve the exact same doctor, date, and time.
     */
    public boolean bookAppointment(Appointment appointment) {
        String lockDoctorSql = "SELECT doctor_id FROM doctors WHERE doctor_id = ? FOR UPDATE";
        String checkSql = "SELECT COUNT(*) FROM appointments WHERE doctor_id = ? AND appointment_date = ? AND appointment_time = ? AND status = 'CONFIRMED'";
        String insertSql = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, status) VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin ACID Transaction

            // Step 1: Acquire database-level exclusive row lock on target doctor
            try (PreparedStatement lockPs = conn.prepareStatement(lockDoctorSql)) {
                lockPs.setInt(1, appointment.getDoctorId());
                try (ResultSet rs = lockPs.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback(); // Doctor not found
                        return false;
                    }
                }
            }

            // Step 2: Check slot availability within the protected database transaction
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setInt(1, appointment.getDoctorId());
                checkPs.setDate(2, appointment.getAppointmentDate());
                checkPs.setString(3, appointment.getAppointmentTime().trim());
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        conn.rollback(); // Conflict detected; abort atomically
                        return false;
                    }
                }
            }

            // Step 3: Insert confirmed booking atomically
            try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                insertPs.setInt(1, appointment.getPatientId());
                insertPs.setInt(2, appointment.getDoctorId());
                insertPs.setDate(3, appointment.getAppointmentDate());
                insertPs.setString(4, appointment.getAppointmentTime().trim());
                insertPs.setString(5, "CONFIRMED");
                int affected = insertPs.executeUpdate();

                if (affected > 0) {
                    conn.commit(); // Commit transaction and release database lock
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
                    System.err.println("[AppointmentDAO] Rollback failed: " + ex.getMessage());
                }
            }
            System.err.println("[AppointmentDAO] Booking transaction error: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("[AppointmentDAO] Connection reset failed: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Retrieves all appointments for a specific logged-in patient.
     * Enforces user authorization by filtering strictly by patientId.
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
            System.err.println("[AppointmentDAO] Patient appointments query failed: " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves all appointments across the hospital for Administrator view.
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
            System.err.println("[AppointmentDAO] Admin appointments query failed: " + e.getMessage());
        }
        return list;
    }

    /**
     * Allows a patient to cancel their own appointment.
     * Enforces ownership strictly at the SQL level via (appointment_id = ? AND patient_id = ?).
     * Prevents any patient from cancelling another patient's appointment.
     */
    public boolean cancelAppointmentByPatient(int appointmentId, int patientId) {
        String sql = "UPDATE appointments SET status = 'CANCELLED' WHERE appointment_id = ? AND patient_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            ps.setInt(2, patientId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AppointmentDAO] Cancel appointment failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Returns total appointment count for a given patient.
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
            System.err.println("[AppointmentDAO] Count query failed: " + e.getMessage());
        }
        return 0;
    }
}
