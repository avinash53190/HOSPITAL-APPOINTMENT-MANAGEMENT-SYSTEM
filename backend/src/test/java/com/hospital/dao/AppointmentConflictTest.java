package com.hospital.dao;

import com.hospital.model.Appointment;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for Appointment booking and double-booking conflict prevention.
 */
public class AppointmentConflictTest {

    @Test
    public void testDoubleBookingSlotConflictPrevention() {
        AppointmentDAO dao = new AppointmentDAO();
        int doctorId = 1;
        Date testDate = Date.valueOf(LocalDate.now().plusDays(10));
        String timeSlot = "11:00 AM";

        // Initial booking attempt 1
        Appointment appt1 = new Appointment(1, doctorId, testDate, timeSlot, "CONFIRMED");
        boolean booked1 = dao.bookAppointment(appt1);

        if (booked1) {
            // Verify slot is now taken
            boolean slotTaken = dao.isSlotTaken(doctorId, testDate, timeSlot);
            assertTrue(slotTaken, "Slot must be detected as taken after confirmed booking");

            // Attempt conflicting booking 2 for same doctor, date, and time
            Appointment appt2 = new Appointment(2, doctorId, testDate, timeSlot, "CONFIRMED");
            boolean booked2 = dao.bookAppointment(appt2);

            assertFalse(booked2, "Second booking for exact same doctor, date and time must be rejected atomically");
        }
    }
}

