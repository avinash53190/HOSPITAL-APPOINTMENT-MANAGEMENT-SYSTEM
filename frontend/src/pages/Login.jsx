import React, { useState } from 'react';
import { Mail, Lock, Eye, EyeOff, ShieldCheck, CheckCircle2, ArrowRight } from 'lucide-react';
import { api } from '../services/api';

export default function Login({ onLoginSuccess, onNavigate, onShowMessage }) {
  const [role, setRole] = useState('PATIENT'); // 'PATIENT' | 'ADMIN'
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!email.trim() || !password) {
      setError('Please provide your credentials.');
      return;
    }

    setLoading(true);
    try {
      const res = await api.login(email.trim(), password, role);
      onShowMessage('success', res.message || 'Authenticated successfully.');
      onLoginSuccess(res.user, res.role);
    } catch (err) {
      setError(err.message || 'Login failed.');
    } finally {
      setLoading(false);
    }
  };

  const handleAdminFill = () => {
    setRole('ADMIN');
    setEmail('admin');
    setPassword('admin123');
    setError('');
  };

  return (
    <div className="auth-page-container">
      <div className="auth-card-split">
        {/* Left Visual Branding Panel */}
        <div className="auth-visual-panel">
          <div className="auth-visual-top">
            <span style={{ fontSize: '11px', textTransform: 'uppercase', letterSpacing: '0.08em', color: 'var(--secondary)', fontWeight: 700 }}>
              CITYCARE PORTAL
            </span>
            <h2 style={{ marginTop: '8px' }}>Manage Your Healthcare Journey.</h2>
            <p>Access your medical appointments, specialist consultations, and historical booking records securely.</p>
          </div>

          <ul className="auth-features-list">
            <li className="auth-feature-item">
              <CheckCircle2 size={16} className="auth-feature-check" />
              <span>Real-time Doctor Availability</span>
            </li>
            <li className="auth-feature-item">
              <CheckCircle2 size={16} className="auth-feature-check" />
              <span>Server-Side Session Authentication</span>
            </li>
            <li className="auth-feature-item">
              <CheckCircle2 size={16} className="auth-feature-check" />
              <span>Instant Appointment Confirmation</span>
            </li>
          </ul>

          <div style={{ fontSize: '12px', color: '#64748B' }}>
            MCA Practical Demonstration &bull; Apache Tomcat Backend
          </div>
        </div>

        {/* Right Form Panel */}
        <div className="auth-form-panel">
          <div className="auth-header">
            <h2>Welcome Back</h2>
            <p>Select your account role to proceed to your dashboard</p>
          </div>

          {/* Role Switcher */}
          <div className="role-tab-group">
            <button
              type="button"
              className={`role-tab ${role === 'PATIENT' ? 'active' : ''}`}
              onClick={() => { setRole('PATIENT'); setError(''); }}
            >
              Patient Sign In
            </button>
            <button
              type="button"
              className={`role-tab ${role === 'ADMIN' ? 'active' : ''}`}
              onClick={() => { setRole('ADMIN'); setError(''); }}
            >
              Administrator
            </button>
          </div>

          {error && (
            <div style={{ background: 'var(--error-bg)', border: '1px solid var(--error-border)', color: 'var(--error)', padding: '10px 14px', borderRadius: 'var(--radius-md)', fontSize: '13px', marginBottom: '16px' }}>
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <div className="form-field">
              <label className="form-label-clean">
                {role === 'ADMIN' ? 'Admin Username' : 'Email Address'}
              </label>
              <div className="input-with-icon">
                <Mail size={16} className="input-icon-left" />
                <input
                  type={role === 'ADMIN' ? 'text' : 'email'}
                  className="input-clean"
                  placeholder={role === 'ADMIN' ? 'admin' : 'patient@example.com'}
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>
            </div>

            <div className="form-field">
              <label className="form-label-clean">Password</label>
              <div className="input-with-icon">
                <Lock size={16} className="input-icon-left" />
                <input
                  type={showPassword ? 'text' : 'password'}
                  className="input-clean"
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
                <button
                  type="button"
                  className="input-password-toggle"
                  onClick={() => setShowPassword(!showPassword)}
                  aria-label="Toggle password visibility"
                >
                  {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
            </div>

            <button
              type="submit"
              className="btn btn-navy"
              style={{ width: '100%', marginTop: '6px' }}
              disabled={loading}
            >
              <span>{loading ? 'Authenticating...' : `Sign In as ${role === 'ADMIN' ? 'Admin' : 'Patient'}`}</span>
              {!loading && <ArrowRight size={15} />}
            </button>
          </form>

          {role === 'ADMIN' && (
            <div style={{ marginTop: '16px', background: '#F8FAFC', border: '1px solid var(--border)', borderRadius: 'var(--radius-sm)', padding: '10px 12px', fontSize: '12px' }}>
              <span style={{ color: 'var(--text-muted)' }}>Viva Demo Credentials: </span>
              <strong>admin</strong> / <strong>admin123</strong>
              <button
                type="button"
                onClick={handleAdminFill}
                style={{ display: 'block', marginTop: '6px', color: 'var(--secondary)', background: 'transparent', border: 'none', cursor: 'pointer', fontWeight: 600, fontSize: '11px', padding: 0 }}
              >
                + Auto-fill Credentials
              </button>
            </div>
          )}

          <div style={{ marginTop: '24px', textAlign: 'center', fontSize: '13px', color: 'var(--text-muted)' }}>
            Need a patient account?{' '}
            <button
              type="button"
              onClick={() => onNavigate('register')}
              style={{ background: 'transparent', border: 'none', color: 'var(--secondary)', fontWeight: 600, cursor: 'pointer' }}
            >
              Create Account
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
