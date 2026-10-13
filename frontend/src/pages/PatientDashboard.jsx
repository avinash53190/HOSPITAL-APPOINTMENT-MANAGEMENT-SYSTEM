import React, { useEffect, useState } from 'react';
import { api } from '../services/api';

export default function PatientDashboard({ auth, onNavigate }) {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      const appts = await api.getAppointments();
      setAppointments(appts || []);
    } catch (err) {
      console.error('Failed to load appointments', err);
    } finally {
      setLoading(false);
    }
  };

  const activeAppointments = appointments.filter(a => a.status === 'CONFIRMED');
  const cancelledAppointments = appointments.filter(a => a.status === 'CANCELLED');

  return (
    <div>
      {/* Welcome Banner */}
      <div className="dashboard-welcome">
        <div className="dashboard-welcome-inner">
          <div>
            <span className="dashboard-eyebrow">
              PATIENT DASHBOARD
            </span>
            <h2>
              Welcome back, {auth.user?.name || 'Patient'}!
            </h2>
            <p>
              Email: {auth.user?.email} | Session Authenticated via Java HttpSession
            </p>
          </div>
          <div className="dashboard-welcome-actions">
            <button
              className="btn-hero-white"
              onClick={() => onNavigate('book')}
            >
              + Book New Appointment
            </button>
            <a
              href="http://localhost:8080/dashboard.jsp"
              target="_blank"
              rel="noopener noreferrer"
              className="btn-hero-outline"
            >
              View in JSP
            </a>
          </div>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon blue">📅</div>
          <div className="stat-info">
            <h3>{appointments.length}</h3>
            <p>Total Bookings</p>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon green">✅</div>
          <div className="stat-info">
            <h3>{activeAppointments.length}</h3>
            <p>Active Appointments</p>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon teal">🩺</div>
          <div className="stat-info">
            <h3>{cancelledAppointments.length}</h3>
            <p>Cancelled Bookings</p>
          </div>
        </div>
      </div>

      {/* Quick Navigation Cards */}
      <div className="section-header">
        <div>
          <h3 className="section-title">Quick Actions</h3>
          <p className="section-subtitle">Manage your hospital visits</p>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '20px', marginBottom: '35px' }}>
        <div
          style={{ background: 'white', padding: '24px', borderRadius: '10px', border: '1px solid #e2e8f0', cursor: 'pointer', transition: 'all 0.2s' }}
          onClick={() => onNavigate('doctors')}
        >
          <div style={{ fontSize: '30px', marginBottom: '10px' }}>👨‍⚕️</div>
          <h4 style={{ fontSize: '18px', fontWeight: 700, color: '#0f172a', marginBottom: '6px' }}>View Doctors</h4>
          <p style={{ fontSize: '13px', color: '#64748b' }}>Explore all specialists and doctor availability.</p>
        </div>

        <div
          style={{ background: 'white', padding: '24px', borderRadius: '10px', border: '1px solid #e2e8f0', cursor: 'pointer', transition: 'all 0.2s' }}
          onClick={() => onNavigate('book')}
        >
          <div style={{ fontSize: '30px', marginBottom: '10px' }}>🗓️</div>
          <h4 style={{ fontSize: '18px', fontWeight: 700, color: '#0f172a', marginBottom: '6px' }}>Book Appointment</h4>
          <p style={{ fontSize: '13px', color: '#64748b' }}>Select a specialist and pick an available time slot.</p>
        </div>

        <div
          style={{ background: 'white', padding: '24px', borderRadius: '10px', border: '1px solid #e2e8f0', cursor: 'pointer', transition: 'all 0.2s' }}
          onClick={() => onNavigate('my-appointments')}
        >
          <div style={{ fontSize: '30px', marginBottom: '10px' }}>📋</div>
          <h4 style={{ fontSize: '18px', fontWeight: 700, color: '#0f172a', marginBottom: '6px' }}>My Appointments</h4>
          <p style={{ fontSize: '13px', color: '#64748b' }}>Check status or cancel upcoming appointments.</p>
        </div>
      </div>

      {/* Recent Appointments Preview */}
      <div className="section-header">
        <div>
          <h3 className="section-title">Recent Appointments</h3>
          <p className="section-subtitle">Your latest medical consultations</p>
        </div>
        <button className="nav-btn btn-secondary" onClick={() => onNavigate('my-appointments')}>
          View All →
        </button>
      </div>

      {loading ? (
        <p style={{ textAlign: 'center', color: '#64748b', padding: '30px' }}>Loading appointments...</p>
      ) : appointments.length === 0 ? (
        <div style={{ background: 'white', padding: '40px', borderRadius: '10px', textAlign: 'center', border: '1px solid #e2e8f0' }}>
          <p style={{ color: '#64748b', fontSize: '15px' }}>You have no appointments booked yet.</p>
          <button className="btn-primary nav-btn" style={{ marginTop: '15px' }} onClick={() => onNavigate('book')}>
            Book Your First Appointment
          </button>
        </div>
      ) : (
        <div className="table-container">
          <table className="custom-table">
            <thead>
              <tr>
                <th>Doctor</th>
                <th>Specialization</th>
                <th>Date</th>
                <th>Time Slot</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {appointments.slice(0, 3).map((appt) => (
                <tr key={appt.appointmentId}>
                  <td><strong>{appt.doctorName}</strong></td>
                  <td>{appt.doctorSpecialization}</td>
                  <td>{appt.appointmentDate}</td>
                  <td>{appt.appointmentTime}</td>
                  <td>
                    <span className={`badge ${appt.status === 'CONFIRMED' ? 'badge-confirmed' : 'badge-cancelled'}`}>
                      {appt.status}
                    </span>
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
