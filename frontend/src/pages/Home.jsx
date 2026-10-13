import React, { useEffect, useState } from 'react';
import { 
  CalendarCheck, 
  Users, 
  Shield, 
  ArrowRight, 
  Stethoscope, 
  Clock, 
  CheckCircle2, 
  ChevronRight,
  HeartPulse
} from 'lucide-react';
import { api } from '../services/api';

export default function Home({ auth, onNavigate, onSelectDoctor }) {
  const [featuredDoctors, setFeaturedDoctors] = useState([]);
  const [loadingDocs, setLoadingDocs] = useState(true);

  useEffect(() => {
    loadDoctors();
  }, []);

  const loadDoctors = async () => {
    try {
      setLoadingDocs(true);
      const data = await api.getDoctors();
      setFeaturedDoctors((data || []).slice(0, 3));
    } catch (err) {
      console.error('Failed to load doctors for homepage', err);
    } finally {
      setLoadingDocs(false);
    }
  };

  const scrollToHowItWorks = () => {
    const el = document.getElementById('how-it-works-section');
    if (el) el.scrollIntoView({ behavior: 'smooth' });
  };

  return (
    <div>
      {/* Hero Section */}
      <section className="hero-section">
        <div>
          <div className="hero-pill">
            <HeartPulse size={14} />
            <span>MODERN HEALTHCARE SCHEDULING</span>
          </div>

          <h1 className="hero-headline">
            Healthcare, <span>Made Simpler.</span>
          </h1>

          <p className="hero-subtext">
            Find trusted specialists and book your next appointment in just a few clicks. Real-time availability without the waiting room hassle.
          </p>

          <div className="hero-actions">
            <button className="btn btn-primary btn-lg" onClick={() => onNavigate('doctors')}>
              <span>Find a Doctor</span>
              <ArrowRight size={16} />
            </button>
            <button className="btn btn-secondary btn-lg" onClick={scrollToHowItWorks}>
              How It Works
            </button>
          </div>
        </div>

        {/* Right-Side Healthcare Visual Card */}
        <div className="hero-visual">
          <div style={{
            width: '100%',
            maxWidth: '340px',
            background: '#FFFFFF',
            borderRadius: 'var(--radius-lg)',
            padding: '24px',
            boxShadow: 'var(--shadow-sm)',
            border: '1px solid var(--border)'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '14px', marginBottom: '16px' }}>
              <div style={{
                width: '48px',
                height: '48px',
                borderRadius: 'var(--radius-md)',
                backgroundColor: 'var(--secondary-light)',
                color: 'var(--secondary)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}>
                <Stethoscope size={24} />
              </div>
              <div>
                <h4 style={{ fontSize: '15px', color: 'var(--primary)', fontWeight: 600 }}>Dr. Rajesh Sharma</h4>
                <p style={{ fontSize: '12px', color: 'var(--secondary)', fontWeight: 500 }}>Chief Cardiologist</p>
              </div>
            </div>

            <div style={{ padding: '12px', background: '#F8FAFC', borderRadius: 'var(--radius-sm)', marginBottom: '16px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', marginBottom: '6px' }}>
                <span style={{ color: 'var(--text-muted)' }}>Status</span>
                <span style={{ color: 'var(--success)', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '4px' }}>
                  <span style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: 'var(--success)' }}></span>
                  Available Today
                </span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px' }}>
                <span style={{ color: 'var(--text-muted)' }}>Next Available Slot</span>
                <span style={{ fontWeight: 600, color: 'var(--primary)' }}>10:00 AM</span>
              </div>
            </div>

            <button
              className="btn btn-navy"
              style={{ width: '100%', fontSize: '13px' }}
              onClick={() => onNavigate('doctors')}
            >
              Book Consultation
            </button>
          </div>

          <div className="hero-badge-card">
            <div style={{ color: 'var(--secondary)' }}>
              <CheckCircle2 size={22} />
            </div>
            <div>
              <div style={{ fontSize: '13px', fontWeight: 600, color: 'var(--primary)' }}>Direct Database Sync</div>
              <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Instant slot reservation with MySQL JDBC</div>
            </div>
          </div>
        </div>
      </section>

      {/* Trust Bar */}
      <div className="trust-bar">
        <div className="trust-item">
          <div className="trust-icon-box">
            <CalendarCheck size={20} />
          </div>
          <div>
            <h4>Easy Appointment Booking</h4>
            <p>Pick doctor, date and time slot in seconds.</p>
          </div>
        </div>

        <div className="trust-item">
          <div className="trust-icon-box">
            <Users size={20} />
          </div>
          <div>
            <h4>Specialist Doctors</h4>
            <p>Direct access to department specialists.</p>
          </div>
        </div>

        <div className="trust-item">
          <div className="trust-icon-box">
            <Shield size={20} />
          </div>
          <div>
            <h4>Secure Patient Access</h4>
            <p>Protected sessions and BCrypt encrypted records.</p>
          </div>
        </div>
      </div>

      {/* Featured Doctors Section */}
      <section style={{ marginBottom: '60px' }}>
        <div className="section-header-compact">
          <div>
            <div className="section-label">DOCTORS DIRECTORY</div>
            <h2 className="section-title-clean">Find the Right Specialist</h2>
          </div>
          <button
            className="btn btn-secondary btn-sm"
            onClick={() => onNavigate('doctors')}
          >
            <span>View All Doctors</span>
            <ChevronRight size={14} />
          </button>
        </div>

        {loadingDocs ? (
          <div className="doctors-grid">
            {[1, 2, 3].map((i) => (
              <div key={i} className="doc-card" style={{ height: '180px' }}>
                <div className="skeleton" style={{ height: '40px', width: '70%', marginBottom: '15px' }} />
                <div className="skeleton" style={{ height: '20px', width: '50%', marginBottom: '20px' }} />
                <div className="skeleton" style={{ height: '36px', width: '100%' }} />
              </div>
            ))}
          </div>
        ) : (
          <div className="doctors-grid">
            {featuredDoctors.map((doc) => (
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
                      {doc.available ? "Available for Consultations" : "Slot Full"}
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
                  Book Appointment
                </button>
              </div>
            ))}
          </div>
        )}
      </section>

      {/* How It Works Section */}
      <section id="how-it-works-section" style={{ marginBottom: '60px' }}>
        <div style={{ textAlign: 'center', marginBottom: '32px' }}>
          <div className="section-label">SIMPLE WORKFLOW</div>
          <h2 className="section-title-clean">How It Works</h2>
          <p style={{ marginTop: '6px' }}>Book your medical consultation in three intuitive steps</p>
        </div>

        <div className="how-it-works-grid">
          <div className="step-card">
            <div className="step-number">1</div>
            <h3>Find a Doctor</h3>
            <p>Browse by medical specialization or search by physician name to find the best match for your needs.</p>
          </div>

          <div className="step-card">
            <div className="step-number">2</div>
            <h3>Choose a Suitable Slot</h3>
            <p>Select your desired date and preferred time slot. Collision prevention guarantees no double-bookings.</p>
          </div>

          <div className="step-card">
            <div className="step-number">3</div>
            <h3>Confirm Appointment</h3>
            <p>Receive immediate booking confirmation stored securely in MySQL with real-time patient dashboard tracking.</p>
          </div>
        </div>
      </section>
    </div>
  );
}
