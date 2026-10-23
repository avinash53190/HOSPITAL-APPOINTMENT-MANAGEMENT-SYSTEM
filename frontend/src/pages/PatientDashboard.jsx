import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import {
  Calendar,
  CheckCircle2,
  XCircle,
  Clock,
  PlusCircle,
  ExternalLink,
  Stethoscope,
  ArrowRight,
  ShieldCheck,
  User
} from 'lucide-react';

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
  const upcomingAppointments = appointments.slice(0, 3);

  return (
    <div className="dashboard-container">
      {/* Welcome Banner */}
      <div className="dashboard-welcome">
        <div className="dashboard-welcome-inner">
          <div>
            <div className="dashboard-badge">
              <ShieldCheck size={14} className="text-teal" />
              <span>HttpSession Protected &bull; MCA Practical Project</span>
            </div>
            <h2>Welcome back, {auth.user?.name || 'Patient'}</h2>
            <p>
              Registered Email: {auth.user?.email} &bull; Manage your consultations, review schedules, and discover specialists.
            </p>
          </div>
          <div className="dashboard-welcome-actions">
            <button
              className="btn btn-primary"
              onClick={() => onNavigate('book')}
            >
              <PlusCircle size={16} />
              <span>Book Appointment</span>
            </button>
            <a
              href="http://localhost:8080/dashboard.jsp"
              target="_blank"
              rel="noopener noreferrer"
              className="btn btn-secondary"
            >
              <ExternalLink size={16} />
              <span>Open JSP Dashboard</span>
            </a>
          </div>
        </div>
      </div>

      {/* Metrics Grid */}
      <div className="dashboard-metrics-grid">
        <div className="metric-card">
          <div className="metric-icon metric-icon-primary">
            <Calendar size={22} />
          </div>
          <div className="metric-details">
            <div className="metric-label">Total Bookings</div>
            <div className="metric-value">{appointments.length}</div>
            <div className="metric-foot">All-time appointments</div>
          </div>
        </div>

        <div className="metric-card">
          <div className="metric-icon metric-icon-success">
            <CheckCircle2 size={22} />
          </div>
          <div className="metric-details">
            <div className="metric-label">Confirmed</div>
            <div className="metric-value">{activeAppointments.length}</div>
            <div className="metric-foot text-success">Active scheduled visits</div>
          </div>
        </div>

        <div className="metric-card">
          <div className="metric-icon metric-icon-danger">
            <XCircle size={22} />
          </div>
          <div className="metric-details">
            <div className="metric-label">Cancelled</div>
            <div className="metric-value">{cancelledAppointments.length}</div>
            <div className="metric-foot text-muted">Past cancelled slots</div>
          </div>
        </div>
      </div>

      {/* Quick Action Navigation Cards */}
      <div className="action-cards-grid">
        <div className="action-card" onClick={() => onNavigate('doctors')}>
          <div className="action-card-header">
            <div className="action-icon">
              <Stethoscope size={24} />
            </div>
            <ArrowRight size={18} className="action-arrow" />
          </div>
          <h3>Find Specialist Doctors</h3>
          <p>Explore all available medical departments, view doctor bios, and select your doctor.</p>
        </div>

        <div className="action-card" onClick={() => onNavigate('book')}>
          <div className="action-card-header">
            <div className="action-icon">
              <PlusCircle size={24} />
            </div>
            <ArrowRight size={18} className="action-arrow" />
          </div>
          <h3>Book an Appointment</h3>
          <p>Choose an available date and verified time slot with atomic conflict prevention.</p>
        </div>

        <div className="action-card" onClick={() => onNavigate('my-appointments')}>
          <div className="action-card-header">
            <div className="action-icon">
              <Clock size={24} />
            </div>
            <ArrowRight size={18} className="action-arrow" />
          </div>
          <h3>My Appointments</h3>
          <p>Review your booking details, filter past schedules, or cancel slots with instant confirmation.</p>
        </div>
      </div>

      {/* Recent Appointments Preview */}
      <div className="dashboard-section">
        <div className="section-header-row">
          <div>
            <h3>Recent Appointments</h3>
            <p className="text-muted">Your latest booking activities</p>
          </div>
          {appointments.length > 0 && (
            <button className="btn btn-ghost" onClick={() => onNavigate('my-appointments')}>
              View All Appointments &rarr;
            </button>
          )}
        </div>

        {loading ? (
          <div className="card loading-placeholder">
            <div className="spinner"></div>
            <p>Loading your appointments...</p>
          </div>
        ) : appointments.length === 0 ? (
          <div className="card empty-card">
            <Calendar size={42} className="text-muted mb-3" />
            <h4>No Appointments Scheduled Yet</h4>
            <p className="text-muted mb-4">Book your first doctor appointment with our board-certified specialists.</p>
            <button className="btn btn-primary" onClick={() => onNavigate('book')}>
              <PlusCircle size={16} />
              <span>Book Appointment Now</span>
            </button>
          </div>
        ) : (
          <div className="card table-card">
            <table className="custom-table">
              <thead>
                <tr>
                  <th>Booking ID</th>
                  <th>Doctor</th>
                  <th>Specialization</th>
                  <th>Date</th>
                  <th>Time Slot</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {upcomingAppointments.map((appt) => (
                  <tr key={appt.appointmentId}>
                    <td><span className="font-mono text-muted">#{appt.appointmentId}</span></td>
                    <td className="font-medium text-navy">{appt.doctorName}</td>
                    <td className="text-muted">{appt.doctorSpecialization}</td>
                    <td>{appt.appointmentDate}</td>
                    <td>{appt.appointmentTime}</td>
                    <td>
                      <span className={`status-pill ${appt.status === 'CONFIRMED' ? 'status-pill-confirmed' : 'status-pill-cancelled'}`}>
                        {appt.status === 'CONFIRMED' ? (
                          <><CheckCircle2 size={12} /> Confirmed</>
                        ) : (
                          <><XCircle size={12} /> Cancelled</>
                        )}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
