import React, { useEffect, useState } from 'react';
import { api } from '../services/api';

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
      setError('Appointment date cannot be in the past.');
      return;
    }
    if (!appointmentTime) {
      setError('Please select a time slot.');
      return;
    }

    setLoading(true);
    try {
      const res = await api.bookAppointment(parseInt(doctorId), appointmentDate, appointmentTime);
      onShowMessage('success', res.message || 'Appointment booked successfully!');
      onNavigate('my-appointments');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="form-card" style={{ maxWidth: '580px' }}>
      <div style={{ textAlign: 'center', marginBottom: '24px' }}>
        <h2 style={{ fontSize: '24px', fontWeight: 700, color: '#0f172a' }}>Book Doctor Appointment</h2>
        <p style={{ fontSize: '13px', color: '#64748b', marginTop: '4px' }}>
          Select a specialist doctor, date, and preferred time slot
        </p>
      </div>

      {error && (
        <div className="alert alert-error">
          <span>⚠️ {error}</span>
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label className="form-label">Select Specialist Doctor</label>
          <select
            className="form-control"
            value={doctorId}
            onChange={(e) => setDoctorId(e.target.value)}
            required
          >
            <option value="">-- Choose Doctor --</option>
            {doctors.map((doc) => (
              <option key={doc.doctorId} value={doc.doctorId}>
                {doc.name} — {doc.specialization} {doc.available ? '(Available)' : '(Busy)'}
              </option>
            ))}
          </select>
        </div>

        <div className="form-group">
          <label className="form-label">Appointment Date</label>
          <input
            type="date"
            className="form-control"
            min={todayStr} // Native HTML5 restriction to prevent past dates
            value={appointmentDate}
            onChange={(e) => setAppointmentDate(e.target.value)}
            required
          />
          <span style={{ fontSize: '11px', color: '#64748b', marginTop: '4px', display: 'block' }}>
            * Note: Past dates are disabled automatically.
          </span>
        </div>

        <div className="form-group">
          <label className="form-label">Preferred Time Slot</label>
          <select
            className="form-control"
            value={appointmentTime}
            onChange={(e) => setAppointmentTime(e.target.value)}
            required
          >
            <option value="">-- Choose Slot --</option>
            {timeSlots.map((slot) => (
              <option key={slot} value={slot}>
                {slot}
              </option>
            ))}
          </select>
          <span style={{ fontSize: '11px', color: '#64748b', marginTop: '4px', display: 'block' }}>
            * Java server automatically checks if this slot is already booked for this doctor.
          </span>
        </div>

        <button
          type="submit"
          className="btn-primary"
          style={{ width: '100%', padding: '12px', borderRadius: '8px', border: 'none', cursor: 'pointer', fontWeight: 700, fontSize: '15px', marginTop: '10px' }}
          disabled={loading}
        >
          {loading ? 'Confirming Booking...' : 'Confirm Appointment'}
        </button>
      </form>
    </div>
  );
}

