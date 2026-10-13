import React, { useEffect, useState } from 'react';
import { api } from '../services/api';

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
      onShowMessage('success', res.message || 'Doctor added successfully!');
      setDocName('');
      setDocSpec('');
      setShowAddModal(false);
    } catch (err) {
      onShowMessage('error', err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      <div className="section-header">
        <div>
          <span style={{ fontSize: '12px', background: '#e0f2fe', color: '#0284c7', padding: '4px 8px', borderRadius: '4px', fontWeight: 700 }}>
            ADMINISTRATOR PORTAL
          </span>
          <h2 className="section-title" style={{ marginTop: '8px' }}>Hospital Control Center</h2>
          <p className="section-subtitle">Oversee all registered doctor schedules and hospital-wide appointments</p>
        </div>
        <button
          className="btn-primary nav-btn"
          onClick={() => setShowAddModal(!showAddModal)}
        >
          {showAddModal ? '✕ Close Form' : '+ Add New Doctor'}
        </button>
      </div>

      {/* Add Doctor Card */}
      {showAddModal && (
        <div className="form-card" style={{ maxWidth: '600px', margin: '0 0 30px 0', borderColor: '#38bdf8' }}>
          <h3 style={{ fontSize: '18px', fontWeight: 700, marginBottom: '15px', color: '#0369a1' }}>
            Register New Hospital Doctor
          </h3>
          <form onSubmit={handleAddDoctor}>
            <div className="form-group">
              <label className="form-label">Doctor Full Name</label>
              <input
                type="text"
                className="form-control"
                placeholder="e.g. Dr. Kavita Deshmukh"
                value={docName}
                onChange={(e) => setDocName(e.target.value)}
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">Medical Specialization</label>
              <input
                type="text"
                className="form-control"
                placeholder="e.g. Cardiology, Neurology, Pediatrics"
                value={docSpec}
                onChange={(e) => setDocSpec(e.target.value)}
                required
              />
            </div>

            <button
              type="submit"
              className="btn-primary"
              style={{ width: '100%', padding: '10px', borderRadius: '8px', border: 'none', cursor: 'pointer', fontWeight: 700 }}
              disabled={submitting}
            >
              {submitting ? 'Saving to Database...' : 'Save Doctor to MySQL'}
            </button>
          </form>
        </div>
      )}

      {/* All Appointments Table */}
      <h3 className="section-title" style={{ fontSize: '18px', marginBottom: '15px' }}>
        All Hospital Appointments ({appointments.length})
      </h3>

      {loading ? (
        <p style={{ textAlign: 'center', padding: '50px', color: '#64748b' }}>Loading records from MySQL...</p>
      ) : appointments.length === 0 ? (
        <div style={{ background: 'white', padding: '40px', textAlign: 'center', borderRadius: '10px', border: '1px solid #e2e8f0' }}>
          <p style={{ color: '#64748b' }}>No hospital appointments registered yet.</p>
        </div>
      ) : (
        <div className="table-container">
          <table className="custom-table">
            <thead>
              <tr>
                <th>Appt #</th>
                <th>Patient Name</th>
                <th>Patient Contact</th>
                <th>Doctor</th>
                <th>Specialization</th>
                <th>Date</th>
                <th>Time</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {appointments.map((appt) => (
                <tr key={appt.appointmentId}>
                  <td>#{appt.appointmentId}</td>
                  <td><strong>{appt.patientName || `Patient #${appt.patientId}`}</strong></td>
                  <td>{appt.patientPhone || 'N/A'}</td>
                  <td>{appt.doctorName}</td>
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

