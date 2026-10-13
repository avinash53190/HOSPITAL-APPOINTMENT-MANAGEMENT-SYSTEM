import React, { useEffect, useState } from 'react';
import { Search, Stethoscope, AlertCircle, Calendar } from 'lucide-react';
import { api } from '../services/api';

export default function DoctorList({ onSelectDoctor, onNavigate, auth }) {
  const [doctors, setDoctors] = useState([]);
  const [search, setSearch] = useState('');
  const [specialization, setSpecialization] = useState('ALL');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchDoctors();
  }, [specialization]);

  const fetchDoctors = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await api.getDoctors(search, specialization);
      setDoctors(data || []);
    } catch (err) {
      setError(err.message || 'Failed to retrieve doctors from backend.');
    } finally {
      setLoading(false);
    }
  };

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    fetchDoctors();
  };

  const specializations = [
    'ALL',
    'Cardiology',
    'Dermatology',
    'Pediatrics',
    'Orthopedics',
    'General Medicine',
    'Neurology'
  ];

  return (
    <div>
      <div className="section-header-compact">
        <div>
          <div className="section-label">SPECIALIST DIRECTORY</div>
          <h2 className="section-title-clean">Find Your Doctor</h2>
          <p style={{ marginTop: '4px', fontSize: '14px' }}>
            Browse available specialists across hospital departments
          </p>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="filter-container">
        <form onSubmit={handleSearchSubmit} className="search-input-wrapper">
          <Search size={16} className="search-icon" />
          <input
            type="text"
            className="search-input"
            placeholder="Search by physician name..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </form>

        <select
          className="select-clean"
          value={specialization}
          onChange={(e) => setSpecialization(e.target.value)}
        >
          {specializations.map((spec) => (
            <option key={spec} value={spec}>
              {spec === 'ALL' ? 'All Specializations' : spec}
            </option>
          ))}
        </select>

        <button type="button" className="btn btn-secondary btn-sm" onClick={fetchDoctors}>
          Apply Filter
        </button>
      </div>

      {/* Error Feedback */}
      {error && (
        <div style={{ background: 'var(--error-bg)', border: '1px solid var(--error-border)', color: 'var(--error)', padding: '14px 18px', borderRadius: 'var(--radius-md)', display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '24px', fontSize: '13px' }}>
          <AlertCircle size={16} />
          <span>{error}</span>
        </div>
      )}

      {/* Loading Skeletons */}
      {loading ? (
        <div className="doctors-grid">
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <div key={i} className="doc-card" style={{ height: '170px' }}>
              <div className="skeleton" style={{ height: '36px', width: '60%', marginBottom: '12px' }} />
              <div className="skeleton" style={{ height: '18px', width: '40%', marginBottom: '24px' }} />
              <div className="skeleton" style={{ height: '36px', width: '100%' }} />
            </div>
          ))}
        </div>
      ) : doctors.length === 0 ? (
        <div className="empty-box" style={{ background: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 'var(--radius-lg)' }}>
          <div className="empty-icon-circle">
            <Search size={22} />
          </div>
          <h4>No doctors matched your criteria</h4>
          <p style={{ color: 'var(--text-muted)' }}>Try adjusting your search query or choosing another medical specialization.</p>
          <button className="btn btn-secondary btn-sm" onClick={() => { setSearch(''); setSpecialization('ALL'); }}>
            Reset Filters
          </button>
        </div>
      ) : (
        <div className="doctors-grid">
          {doctors.map((doc) => (
            <div key={doc.doctorId} className="doc-card">
              <div>
                <div className="doc-card-top">
                  <div className="doc-avatar">
                    <Stethoscope size={22} />
                  </div>
                  <div className="doc-info">
                    <h3>{doc.name}</h3>
                    <span className="spec-badge">{doc.specialization}</span>
                  </div>
                </div>

                <div className="doc-status-line">
                  <span className={doc.available ? "status-dot-active" : "status-dot-inactive"} />
                  <span style={{ color: doc.available ? "var(--success)" : "var(--text-muted)", fontWeight: 500 }}>
                    {doc.available ? "Available for Appointments" : "Fully Booked"}
                  </span>
                </div>
              </div>

              <button
                className="btn btn-primary"
                style={{ width: '100%' }}
                onClick={() => {
                  if (onSelectDoctor) onSelectDoctor(doc);
                  if (auth.authenticated) {
                    onNavigate('book');
                  } else {
                    onNavigate('login');
                  }
                }}
              >
                <Calendar size={14} />
                <span>Book Appointment</span>
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
