import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import {
  ShieldCheck,
  PlusCircle,
  XCircle,
  Calendar,
  Clock,
  User,
  Phone,
  Stethoscope,
  CheckCircle2,
  FileText,
  UserPlus,
  Activity
} from 'lucide-react';

export default function AdminPortal({ onShowMessage }) {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showAddModal, setShowAddModal] = useState(false);

  // Form for adding doctor
  const [docName, setDocName] = useState('');
  const [docSpec, setDocSpec] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchAllAppointments();
  }, []);

  const fetchAllAppointments = async () => {
    try {
      setLoading(true);
      const data = await api.getAppointments();
      setAppointments(data || []);
    } catch (err) {
      console.error('Failed to fetch admin appointments', err);
    } finally {
      setLoading(false);
    }
  };

  const handleAddDoctor = async (e) => {
    e.preventDefault();
    if (!docName.trim() || !docSpec.trim()) return;

    setSubmitting(true);
    try {
      const res = await api.addDoctor(docName.trim(), docSpec.trim(), true);
      onShowMessage('success', res.message || 'Specialist doctor added successfully!');
      setDocName('');
      setDocSpec('');
      setShowAddModal(false);
    } catch (err) {
      onShowMessage('error', err.message);
    } finally {
      setSubmitting(false);
    }
  };

  const totalAppts = appointments.length;
  const confirmedAppts = appointments.filter(a => a.status === 'CONFIRMED').length;
  const cancelledAppts = appointments.filter(a => a.status === 'CANCELLED').length;

  return (
    <div className="admin-portal-container">
      {/* Page Header */}
      <div className="section-header-row mb-4">
        <div>
          <div className="dashboard-badge">
            <ShieldCheck size={14} className="text-teal" />
            <span>Administrator Control Center &bull; Full Hospital Access</span>
          </div>
          <h2 className="page-title mt-2">Hospital Administration Portal</h2>
          <p className="page-subtitle">Oversee all registered doctor schedules, patient visits, and add medical specialists</p>
        </div>
        <button
          className="btn btn-primary"
          onClick={() => setShowAddModal(!showAddModal)}
        >
          {showAddModal ? (
            <>
              <XCircle size={16} />
              <span>Close Form</span>
            </>
          ) : (
            <>
              <UserPlus size={16} />
              <span>Add Specialist Doctor</span>
            </>
          )}
        </button>
      </div>

      {/* Metrics Row */}
      <div className="dashboard-metrics-grid mb-4">
        <div className="metric-card">
          <div className="metric-icon metric-icon-primary">
            <Activity size={22} />
          </div>
          <div className="metric-details">
            <div className="metric-label">Hospital Appointments</div>
            <div className="metric-value">{totalAppts}</div>
            <div className="metric-foot">Total recorded bookings</div>
          </div>
        </div>

        <div className="metric-card">
          <div className="metric-icon metric-icon-success">
            <CheckCircle2 size={22} />
          </div>
          <div className="metric-details">
            <div className="metric-label">Confirmed Visits</div>
            <div className="metric-value">{confirmedAppts}</div>
            <div className="metric-foot text-success">Active scheduled visits</div>
          </div>
        </div>

        <div className="metric-card">
          <div className="metric-icon metric-icon-danger">
            <XCircle size={22} />
          </div>
          <div className="metric-details">
            <div className="metric-label">Cancelled Slots</div>
            <div className="metric-value">{cancelledAppts}</div>
            <div className="metric-foot text-muted">Patient cancelled visits</div>
          </div>
        </div>
      </div>

      {/* Add Doctor Form Panel */}
      {showAddModal && (
        <div className="card add-doctor-panel mb-4">
          <div className="card-header-clean">
            <div className="inline-flex-item">
              <UserPlus size={20} className="text-teal" />
              <h3>Register New Medical Specialist</h3>
            </div>
            <p className="text-muted text-sm">Add a board-certified doctor to the hospital booking directory.</p>
          </div>

          <form onSubmit={handleAddDoctor} className="mt-4">
            <div className="form-grid-2">
              <div className="form-group">
                <label className="form-label">
                  <User size={14} />
                  <span>Doctor Full Name</span>
                </label>
                <input
                  type="text"
                  className="form-control"
                  placeholder="e.g. Dr. Rajesh Sharma, MD"
                  value={docName}
                  onChange={(e) => setDocName(e.target.value)}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">
                  <Stethoscope size={14} />
                  <span>Specialization / Department</span>
                </label>
                <input
                  type="text"
                  className="form-control"
                  placeholder="e.g. Cardiology, Neurology, Pediatrics"
                  value={docSpec}
                  onChange={(e) => setDocSpec(e.target.value)}
                  required
                />
              </div>
            </div>

            <div className="form-actions mt-4">
              <button
                type="submit"
                className="btn btn-primary"
                disabled={submitting || !docName.trim() || !docSpec.trim()}
              >
                {submitting ? (
                  <>
                    <div className="spinner-xs"></div>
                    <span>Saving Doctor Profile...</span>
                  </>
                ) : (
                  <>
                    <CheckCircle2 size={16} />
                    <span>Save Doctor to System</span>
                  </>
                )}
              </button>
              <button
                type="button"
                className="btn btn-ghost"
                onClick={() => setShowAddModal(false)}
              >
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}

      {/* All Appointments Table */}
      <div className="card table-card">
        <div className="card-header-clean">
          <div className="inline-flex-item">
            <FileText size={18} className="text-teal" />
            <h3>Hospital-Wide Patient Bookings</h3>
          </div>
          <span className="text-muted text-sm">Real-time appointments from all registered patients</span>
        </div>

        {loading ? (
          <div className="loading-placeholder">
            <div className="spinner"></div>
            <p>Loading hospital bookings...</p>
          </div>
        ) : appointments.length === 0 ? (
          <div className="empty-card">
            <Calendar size={44} className="text-muted mb-3" />
            <h4>No Appointments in System</h4>
            <p className="text-muted">When patients book consultations, they will appear here in real-time.</p>
          </div>
        ) : (
          <div className="table-responsive">
            <table className="custom-table">
              <thead>
                <tr>
                  <th>Booking ID</th>
                  <th>Patient Name & Phone</th>
                  <th>Assigned Specialist</th>
                  <th>Scheduled Date</th>
                  <th>Time Slot</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {appointments.map((appt) => (
                  <tr key={appt.appointmentId}>
                    <td>
                      <span className="font-mono text-muted">#{appt.appointmentId}</span>
                    </td>
                    <td>
                      <div>
                        <div className="font-semibold text-navy">{appt.patientName || `Patient #${appt.patientId}`}</div>
                        {appt.patientPhone && (
                          <div className="inline-flex-item text-xs text-muted mt-1">
                            <Phone size={11} />
                            <span>{appt.patientPhone}</span>
                          </div>
                        )}
                      </div>
                    </td>
                    <td>
                      <div>
                        <div className="font-medium text-navy">{appt.doctorName}</div>
                        <span className="specialization-tag">{appt.doctorSpecialization}</span>
                      </div>
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
