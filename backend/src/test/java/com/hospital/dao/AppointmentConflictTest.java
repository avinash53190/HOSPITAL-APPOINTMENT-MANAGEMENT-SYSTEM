package com.hospital.dao;

import com.hospital.model.Appointment;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for Appointment booking and database-level concurrency control.
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

    @Test
    public void testConcurrentRaceConditionPrevention() throws InterruptedException {
        AppointmentDAO dao = new AppointmentDAO();
        int doctorId = 2;
        Date testDate = Date.valueOf(LocalDate.now().plusDays(20));
        String timeSlot = "03:00 PM";

        int threadCount = 4;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int patientId = i + 1;
            executor.submit(() -> {
                try {
                    startLatch.await(); // simultaneous trigger
                    Appointment appt = new Appointment(patientId, doctorId, testDate, timeSlot, "CONFIRMED");
                    if (dao.bookAppointment(appt)) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception ignored) {
                }
            });
        }

        startLatch.countDown(); // release all threads at once
        executor.shutdown();
        boolean finished = executor.awaitTermination(5, TimeUnit.SECONDS);
        assertTrue(finished, "All concurrent booking threads must complete within timeout");

        // Exactly one concurrent booking must succeed, others must be rejected by database transaction
        assertEquals(1, successCount.get(), "Database transactions must guarantee that exactly 1 booking succeeds under concurrent attempts");
    }
}
