import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import {
  Calendar,
  Clock,
  CheckCircle2,
  XCircle,
  PlusCircle,
  Stethoscope,
  Filter,
  AlertTriangle
} from 'lucide-react';

export default function MyAppointments({ onNavigate, onShowMessage }) {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(null);
  const [filter, setFilter] = useState('ALL'); // 'ALL' | 'CONFIRMED' | 'CANCELLED'

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
    const confirmCancel = window.confirm(
      'Are you sure you want to cancel this appointment? This action cannot be reversed.'
    );
    if (!confirmCancel) return;

    setActionLoading(appointmentId);
    try {
      const res = await api.cancelAppointment(appointmentId);
      onShowMessage('success', res.message || 'Appointment cancelled successfully.');
      fetchMyAppointments();
    } catch (err) {
      onShowMessage('error', err.message);
    } finally {
      setActionLoading(null);
    }
  };

  const filteredAppointments = appointments.filter((appt) => {
    if (filter === 'CONFIRMED') return appt.status === 'CONFIRMED';
    if (filter === 'CANCELLED') return appt.status === 'CANCELLED';
    return true;
  });

  return (
    <div className="appointments-page-container">
      {/* Header */}
      <div className="section-header-row mb-4">
        <div>
          <h2 className="page-title">My Appointments</h2>
          <p className="page-subtitle">Track, review, or cancel your scheduled hospital consultations</p>
        </div>
        <button className="btn btn-primary" onClick={() => onNavigate('book')}>
          <PlusCircle size={16} />
          <span>Book New Appointment</span>
        </button>
      </div>

      {/* Filter Tabs */}
      <div className="filter-tabs-row mb-4">
        <button
          className={`filter-tab ${filter === 'ALL' ? 'active' : ''}`}
          onClick={() => setFilter('ALL')}
        >
          All Appointments ({appointments.length})
        </button>
        <button
          className={`filter-tab ${filter === 'CONFIRMED' ? 'active' : ''}`}
          onClick={() => setFilter('CONFIRMED')}
        >
          Confirmed ({appointments.filter(a => a.status === 'CONFIRMED').length})
        </button>
        <button
          className={`filter-tab ${filter === 'CANCELLED' ? 'active' : ''}`}
          onClick={() => setFilter('CANCELLED')}
        >
          Cancelled ({appointments.filter(a => a.status === 'CANCELLED').length})
        </button>
      </div>

      {/* Content */}
      {loading ? (
        <div className="card loading-placeholder">
          <div className="spinner"></div>
          <p>Retrieving your scheduled appointments...</p>
        </div>
      ) : appointments.length === 0 ? (
        <div className="card empty-card">
          <Calendar size={48} className="text-muted mb-3" />
          <h3>No Appointments Found</h3>
          <p className="text-muted mb-4">
            You do not currently have any scheduled appointments. Book a consultation with a specialist now.
          </p>
          <button className="btn btn-primary" onClick={() => onNavigate('book')}>
            <PlusCircle size={16} />
            <span>Book Doctor Consultation</span>
          </button>
        </div>
      ) : filteredAppointments.length === 0 ? (
        <div className="card empty-card">
          <Filter size={40} className="text-muted mb-3" />
          <h3>No {filter.toLowerCase()} appointments found</h3>
          <p className="text-muted mb-3">Try changing the filter tabs above.</p>
          <button className="btn btn-secondary" onClick={() => setFilter('ALL')}>
            Show All Appointments
          </button>
        </div>
      ) : (
        <div className="card table-card">
          <div className="table-responsive">
            <table className="custom-table">
              <thead>
                <tr>
                  <th>Booking ID</th>
                  <th>Doctor</th>
                  <th>Department</th>
                  <th>Scheduled Date</th>
                  <th>Time Slot</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredAppointments.map((appt) => (
                  <tr key={appt.appointmentId}>
                    <td>
                      <span className="font-mono text-muted">#{appt.appointmentId}</span>
                    </td>
                    <td>
                      <div className="doctor-cell">
                        <div className="doctor-cell-icon">
                          <Stethoscope size={16} />
                        </div>
                        <span className="font-semibold text-navy">{appt.doctorName}</span>
                      </div>
                    </td>
                    <td>
                      <span className="specialization-tag">{appt.doctorSpecialization}</span>
                    </td>
                    <td>
                      <div className="inline-flex-item">
                        <Calendar size={14} className="text-teal" />
                        <span>{appt.appointmentDate}</span>
                      </div>
                    </td>
                    <td>
                      <div className="inline-flex-item">
                        <Clock size={14} className="text-muted" />
                        <span>{appt.appointmentTime}</span>
                      </div>
                    </td>
                    <td>
                      <span className={`status-pill ${appt.status === 'CONFIRMED' ? 'status-pill-confirmed' : 'status-pill-cancelled'}`}>
                        {appt.status === 'CONFIRMED' ? (
                          <><CheckCircle2 size={12} /> Confirmed</>
                        ) : (
                          <><XCircle size={12} /> Cancelled</>
                        )}
                      </span>
                    </td>
                    <td>
                      {appt.status === 'CONFIRMED' ? (
                        <button
                          className="btn btn-outline-danger btn-sm"
                          disabled={actionLoading === appt.appointmentId}
                          onClick={() => handleCancel(appt.appointmentId)}
                        >
                          {actionLoading === appt.appointmentId ? (
                            <>
                              <div className="spinner-xs"></div>
                              <span>Cancelling...</span>
                            </>
                          ) : (
                            <>
                              <XCircle size={14} />
                              <span>Cancel</span>
                            </>
                          )}
                        </button>
                      ) : (
                        <span className="text-muted text-xs italic">Cancelled</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
