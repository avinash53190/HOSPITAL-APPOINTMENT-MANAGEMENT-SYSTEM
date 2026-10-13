import React, { useState } from 'react';
import { 
  Activity, 
  Calendar, 
  User, 
  LogIn, 
  LogOut, 
  ShieldCheck, 
  Menu, 
  X, 
  ExternalLink,
  Users
} from 'lucide-react';

export default function Navbar({ auth, onNavigate, activeTab, onLogout }) {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const handleNav = (tab) => {
    onNavigate(tab);
    setMobileMenuOpen(false);
  };

  return (
    <nav className="navbar">
      <div className="nav-container">
        {/* Brand */}
        <div className="brand" onClick={() => handleNav('home')}>
          <div className="brand-logo">
            <Activity size={20} strokeWidth={2.5} />
          </div>
          <div>
            <div className="brand-name">
              CityCare
            </div>
            <div className="brand-sub">Healthcare</div>
          </div>
        </div>

        {/* Desktop Navigation */}
        <ul className="nav-links">
          <li>
            <button
              className={`nav-link ${activeTab === 'home' ? 'active' : ''}`}
              onClick={() => handleNav('home')}
            >
              Home
            </button>
          </li>
          <li>
            <button
              className={`nav-link ${activeTab === 'doctors' ? 'active' : ''}`}
              onClick={() => handleNav('doctors')}
            >
              Doctors
            </button>
          </li>

          {auth.authenticated && auth.role === 'PATIENT' && (
            <>
              <li>
                <button
                  className={`nav-link ${activeTab === 'dashboard' ? 'active' : ''}`}
                  onClick={() => handleNav('dashboard')}
                >
                  Dashboard
                </button>
              </li>
              <li>
                <button
                  className={`nav-link ${activeTab === 'my-appointments' ? 'active' : ''}`}
                  onClick={() => handleNav('my-appointments')}
                >
                  <Calendar size={15} />
                  My Appointments
                </button>
              </li>
            </>
          )}

          {auth.authenticated && auth.role === 'ADMIN' && (
            <li>
              <button
                className={`nav-link ${activeTab === 'admin' ? 'active' : ''}`}
                onClick={() => handleNav('admin')}
              >
                <ShieldCheck size={16} />
                Admin Portal
              </button>
            </li>
          )}

          <div className="nav-divider" />

          {/* Syllabus Requirement: Link to JSP Dashboard */}
          <li>
            <a
              href="http://localhost:8080/dashboard.jsp"
              target="_blank"
              rel="noopener noreferrer"
              className="nav-link"
              title="Open protected dashboard rendered by Apache Tomcat JSP Engine"
            >
              <span>JSP Portal</span>
              <ExternalLink size={13} style={{ opacity: 0.7 }} />
            </a>
          </li>

          {/* Auth Controls */}
          {auth.authenticated ? (
            <>
              <li style={{ padding: '0 4px' }}>
                <span style={{ fontSize: '13px', fontWeight: 600, color: 'var(--primary)', display: 'inline-flex', alignItems: 'center', gap: '6px' }}>
                  <User size={15} style={{ color: 'var(--secondary)' }} />
                  {auth.user?.name || 'User'}
                </span>
              </li>
              <li>
                <button
                  className="btn btn-secondary btn-sm"
                  onClick={onLogout}
                  title="Sign out and invalidate session"
                >
                  <LogOut size={14} />
                  <span>Logout</span>
                </button>
              </li>
            </>
          ) : (
            <>
              <li>
                <button
                  className={`nav-link ${activeTab === 'login' ? 'active' : ''}`}
                  onClick={() => handleNav('login')}
                >
                  <LogIn size={15} />
                  <span>Sign In</span>
                </button>
              </li>
              <li>
                <button
                  className="btn btn-primary btn-sm"
                  onClick={() => handleNav('register')}
                >
                  Register
                </button>
              </li>
            </>
          )}
        </ul>

        {/* Mobile menu button */}
        <button
          className="mobile-menu-btn"
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          aria-label="Toggle navigation menu"
        >
          {mobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
        </button>
      </div>

      {/* Mobile Menu Dropdown */}
      {mobileMenuOpen && (
        <div style={{ background: '#FFFFFF', borderBottom: '1px solid var(--border)', padding: '16px 24px', display: 'flex', flexDirection: 'column', gap: '10px' }}>
          <button className="nav-link" onClick={() => handleNav('home')}>Home</button>
          <button className="nav-link" onClick={() => handleNav('doctors')}>Doctors</button>
          {auth.authenticated && auth.role === 'PATIENT' && (
            <>
              <button className="nav-link" onClick={() => handleNav('dashboard')}>Dashboard</button>
              <button className="nav-link" onClick={() => handleNav('my-appointments')}>My Appointments</button>
            </>
          )}
          {auth.authenticated && auth.role === 'ADMIN' && (
            <button className="nav-link" onClick={() => handleNav('admin')}>Admin Portal</button>
          )}
          <a
            href="http://localhost:8080/dashboard.jsp"
            target="_blank"
            rel="noopener noreferrer"
            className="nav-link"
          >
            JSP Portal ↗
          </a>
          {auth.authenticated ? (
            <button className="btn btn-danger-outline btn-sm" onClick={() => { onLogout(); setMobileMenuOpen(false); }}>
              Sign Out
            </button>
          ) : (
            <div style={{ display: 'flex', gap: '8px', marginTop: '6px' }}>
              <button className="btn btn-secondary btn-sm" style={{ flex: 1 }} onClick={() => handleNav('login')}>Sign In</button>
              <button className="btn btn-primary btn-sm" style={{ flex: 1 }} onClick={() => handleNav('register')}>Register</button>
            </div>
          )}
        </div>
      )}
    </nav>
  );
}
