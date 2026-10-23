import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import {
  Calendar,
  Clock,
  User,
  Stethoscope,
  AlertCircle,
  CheckCircle2,
  ArrowLeft,
  CalendarCheck
} from 'lucide-react';

export default function BookAppointment({ selectedDoctor, onNavigate, onShowMessage }) {
  const [doctors, setDoctors] = useState([]);
  const [doctorId, setDoctorId] = useState(selectedDoctor ? selectedDoctor.doctorId : '');
  const [appointmentDate, setAppointmentDate] = useState('');
  const [appointmentTime, setAppointmentTime] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Calculate today's date in YYYY-MM-DD format to prevent past dates
  const todayStr = new Date().toISOString().split('T')[0];

  const timeSlots = [
    '09:00 AM',
    '10:00 AM',
    '11:00 AM',
    '12:00 PM',
    '02:00 PM',
    '03:00 PM',
    '04:00 PM',
    '05:00 PM'
  ];

  useEffect(() => {
    loadDoctors();
  }, []);

  useEffect(() => {
    if (selectedDoctor) {
      setDoctorId(selectedDoctor.doctorId);
    }
  }, [selectedDoctor]);

  const loadDoctors = async () => {
    try {
      const data = await api.getDoctors();
      setDoctors(data || []);
      if (!doctorId && data && data.length > 0) {
        setDoctorId(data[0].doctorId);
      }
    } catch (err) {
      console.error('Error fetching doctors', err);
    }
  };

  const currentDoctor = doctors.find(d => String(d.doctorId) === String(doctorId)) || selectedDoctor;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!doctorId) {
      setError('Please select a doctor.');
      return;
    }
    if (!appointmentDate) {
      setError('Please select an appointment date.');
      return;
    }
    if (appointmentDate < todayStr) {
      setError('Appointment date cannot be in the past. Please select today or a future date.');
      return;
    }
    if (!appointmentTime) {
      setError('Please select a preferred consultation time slot.');
      return;
    }

    setLoading(true);
    try {
      const res = await api.bookAppointment(parseInt(doctorId), appointmentDate, appointmentTime);
      onShowMessage('success', res.message || 'Appointment booked successfully!');
      onNavigate('my-appointments');
    } catch (err) {
      setError(err.message || 'Failed to book appointment. Please try a different slot.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="book-appointment-wrapper">
      <div className="page-header-row mb-4">
        <button className="btn btn-ghost" onClick={() => onNavigate('doctors')}>
          <ArrowLeft size={16} />
          <span>Back to Specialists</span>
        </button>
      </div>

      <div className="book-appointment-card">
        <div className="book-header">
          <div className="book-icon-badge">
            <CalendarCheck size={24} />
          </div>
          <h2>Schedule Doctor Consultation</h2>
          <p className="text-muted">
            Select your medical specialist, choose an appointment date, and reserve an available time slot.
          </p>
        </div>

        {error && (
          <div className="alert alert-error mb-4">
            <AlertCircle size={18} />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="book-form">
          {/* Doctor Selection */}
          <div className="form-group mb-4">
            <label className="form-label">
              <Stethoscope size={15} />
              <span>Select Specialist Doctor</span>
            </label>
            <select
              className="form-control form-select-lg"
              value={doctorId}
              onChange={(e) => setDoctorId(e.target.value)}
              required
            >
              {doctors.map((doc) => (
                <option key={doc.doctorId} value={doc.doctorId}>
                  {doc.name} — {doc.specialization} {doc.available ? '(Available)' : '(Unavailable)'}
                </option>
              ))}
            </select>
          </div>

          {/* Selected Doctor Summary Pill */}
          {currentDoctor && (
            <div className="doctor-summary-box mb-4">
              <div className="doctor-avatar-sm">
                {currentDoctor.profileImage ? (
                  <img src={currentDoctor.profileImage} alt={currentDoctor.name} />
                ) : (
                  <User size={20} />
                )}
              </div>
              <div className="doctor-summary-info">
                <h4>{currentDoctor.name}</h4>
                <p>{currentDoctor.specialization}</p>
              </div>
              <div className="doctor-summary-status">
                <span className="availability-dot"></span> Available
              </div>
            </div>
          )}

          {/* Date Selection */}
          <div className="form-group mb-4">
            <label className="form-label">
              <Calendar size={15} />
              <span>Appointment Date (Today or Future)</span>
            </label>
            <input
              type="date"
              className="form-control"
              min={todayStr}
              value={appointmentDate}
              onChange={(e) => setAppointmentDate(e.target.value)}
              required
            />
            <span className="form-hint">
              Past dates are restricted by server and client validations.
            </span>
          </div>

          {/* Time Slot Selection */}
          <div className="form-group mb-4">
            <label className="form-label">
              <Clock size={15} />
              <span>Select Consultation Time Slot</span>
            </label>
            <div className="time-slots-grid">
              {timeSlots.map((slot) => {
                const isSelected = appointmentTime === slot;
                return (
                  <button
                    key={slot}
                    type="button"
                    className={`time-slot-btn ${isSelected ? 'time-slot-selected' : ''}`}
                    onClick={() => setAppointmentTime(slot)}
                  >
                    <Clock size={13} />
                    <span>{slot}</span>
                  </button>
                );
              })}
            </div>
            {appointmentTime && (
              <div className="selected-slot-feedback">
                Selected: <strong>{appointmentTime}</strong>
              </div>
            )}
          </div>

          {/* Submit Button */}
          <div className="form-actions mt-4">
            <button
              type="submit"
              className="btn btn-primary btn-block btn-lg"
              disabled={loading || !doctorId || !appointmentDate || !appointmentTime}
            >
              {loading ? (
                <>
                  <div className="spinner-sm"></div>
                  <span>Reserving Slot...</span>
                </>
              ) : (
                <>
                  <CheckCircle2 size={18} />
                  <span>Confirm & Book Appointment</span>
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
