import React, { useEffect, useState } from 'react';
import { api } from '../services/api';

export default function MyAppointments({ onNavigate, onShowMessage }) {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);

  useEffect(() => {
    fetchMyAppointments();
  }, []);

  const fetchMyAppointments = async () => {
    try {
      setLoading(true);
      const data = await api.getAppointments();
      setAppointments(data || []);
    } catch (err) {
      console.error('Error fetching appointments', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = async (appointmentId) => {
    const confirmCancel = window.confirm('Are you sure you want to cancel this appointment?');
    if (!confirmCancel) return;

    setActionLoading(true);
    try {
      const res = await api.cancelAppointment(appointmentId);
      onShowMessage('success', res.message || 'Appointment cancelled successfully.');
      fetchMyAppointments();
    } catch (err) {
      onShowMessage('error', err.message);
    } finally {
      setActionLoading(false);
    }
  };

  return (
    <div>
      <div className="section-header">
        <div>
          <h2 className="section-title">My Appointments</h2>
          <p className="section-subtitle">Track, review, or cancel your scheduled visits</p>
        </div>
        <button className="btn-primary nav-btn" onClick={() => onNavigate('book')}>
          + Book New Appointment
        </button>
      </div>

      {loading ? (
        <p style={{ textAlign: 'center', padding: '50px', color: '#64748b' }}>Loading your appointments...</p>
      ) : appointments.length === 0 ? (
        <div style={{ background: 'white', padding: '50px', textAlign: 'center', borderRadius: '10px', border: '1px solid #e2e8f0' }}>
          <div style={{ fontSize: '48px', marginBottom: '15px' }}>📅</div>
          <h3 style={{ fontSize: '18px', fontWeight: 700, marginBottom: '8px' }}>No Appointments Found</h3>
          <p style={{ color: '#64748b', fontSize: '14px', marginBottom: '20px' }}>
            You do not currently have any scheduled appointments.
          </p>
          <button className="btn-primary nav-btn" onClick={() => onNavigate('book')}>
            Book Doctor Consultation
          </button>
        </div>
      ) : (
        <div className="table-container">
          <table className="custom-table">
            <thead>
              <tr>
                <th>Appt #</th>
                <th>Doctor</th>
                <th>Specialization</th>
                <th>Date</th>
                <th>Time Slot</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {appointments.map((appt) => (
                <tr key={appt.appointmentId}>
                  <td>#{appt.appointmentId}</td>
                  <td><strong>{appt.doctorName}</strong></td>
                  <td>{appt.doctorSpecialization}</td>
                  <td>{appt.appointmentDate}</td>
                  <td>{appt.appointmentTime}</td>
                  <td>
                    <span className={`badge ${appt.status === 'CONFIRMED' ? 'badge-confirmed' : 'badge-cancelled'}`}>
                      {appt.status}
                    </span>
                  </td>
                  <td>
                    {appt.status === 'CONFIRMED' ? (
                      <button
                        className="btn-danger nav-btn"
                        style={{ fontSize: '12px', padding: '5px 12px' }}
                        disabled={actionLoading}
                        onClick={() => handleCancel(appt.appointmentId)}
                      >
                        Cancel
                      </button>
                    ) : (
                      <span style={{ fontSize: '12px', color: '#94a3b8' }}>Cancelled</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

