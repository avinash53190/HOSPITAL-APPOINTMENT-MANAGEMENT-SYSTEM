import React, { useState } from 'react';
import { Mail, Lock, Eye, EyeOff, ShieldCheck, CheckCircle2, ArrowRight, UserPlus, AlertCircle } from 'lucide-react';
import { api } from '../services/api';

export default function Login({ onLoginSuccess, onNavigate, onShowMessage }) {
  const [role, setRole] = useState('PATIENT'); // 'PATIENT' | 'ADMIN'
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Initial Admin Setup State (only available on first-time deployment)
  const [showSetup, setShowSetup] = useState(false);
  const [setupUser, setSetupUser] = useState('');
  const [setupPass, setSetupPass] = useState('');
  const [setupLoading, setSetupLoading] = useState(false);
  const [setupError, setSetupError] = useState('');

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
      if (err.message && err.message.toLowerCase().includes('no administrator account')) {
        setShowSetup(true);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleAdminSetup = async (e) => {
    e.preventDefault();
    setSetupError('');

    if (!setupUser.trim() || !setupPass) {
      setSetupError('Username and password are required.');
      return;
    }
    if (setupPass.length < 6) {
      setSetupError('Administrator password must be at least 6 characters.');
      return;
    }

    setSetupLoading(true);
    try {
      const res = await api.setupAdmin(setupUser.trim(), setupPass);
      onShowMessage('success', res.message || 'Administrator initialized successfully.');
      setEmail(setupUser.trim());
      setPassword(setupPass);
      setShowSetup(false);
      setRole('ADMIN');
      setError('');
    } catch (err) {
      setSetupError(err.message || 'Setup failed. Administrator account may already be initialized.');
    } finally {
      setSetupLoading(false);
    }
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
              <span>BCrypt Password Security</span>
            </li>
          </ul>

          <div style={{ fontSize: '12px', color: '#64748B' }}>
            MCA Practical Demonstration &bull; Apache Tomcat Backend
          </div>
        </div>

        {/* Right Form Panel */}
        <div className="auth-form-panel">
          <div className="auth-header">
            <h2>{showSetup ? 'First-Time Admin Setup' : 'Welcome Back'}</h2>
            <p>
              {showSetup
                ? 'Create the primary administrator account (enabled only once on initial setup)'
                : 'Select your account role to proceed to your dashboard'}
            </p>
          </div>

          {!showSetup ? (
            <>
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

              <form onSubmit={handleSubmit} className="auth-form">
                <div className="form-group">
                  <label className="form-label">
                    {role === 'ADMIN' ? 'Administrator Username' : 'Email Address'}
                  </label>
                  <div className="input-with-icon">
                    <span className="input-icon-slot">
                      <Mail size={16} />
                    </span>
                    <input
                      type={role === 'ADMIN' ? 'text' : 'email'}
                      className="form-control"
                      placeholder={role === 'ADMIN' ? 'Enter admin username' : 'name@example.com'}
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      required
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label className="form-label">Password</label>
                  <div className="input-with-icon">
                    <span className="input-icon-slot">
                      <Lock size={16} />
                    </span>
                    <input
                      type={showPassword ? 'text' : 'password'}
                      className="form-control"
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
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                    <span style={{ color: 'var(--text-muted)' }}>First-time setup?</span>
                    <button
                      type="button"
                      onClick={() => { setShowSetup(true); setSetupError(''); }}
                      style={{ color: 'var(--secondary)', background: 'transparent', border: 'none', cursor: 'pointer', fontWeight: 600, fontSize: '11px', padding: 0 }}
                    >
                      + Initialize Admin
                    </button>
                  </div>
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
            </>
          ) : (
            <>
              {setupError && (
                <div style={{ background: 'var(--error-bg)', border: '1px solid var(--error-border)', color: 'var(--error)', padding: '10px 14px', borderRadius: 'var(--radius-md)', fontSize: '13px', marginBottom: '16px' }}>
                  {setupError}
                </div>
              )}

              <form onSubmit={handleAdminSetup} className="auth-form">
                <div className="form-group">
                  <label className="form-label">New Administrator Username</label>
                  <input
                    type="text"
                    className="form-control"
                    placeholder="e.g. admin or head_doctor"
                    value={setupUser}
                    onChange={(e) => setSetupUser(e.target.value)}
                    required
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">New Administrator Password (min 6 characters)</label>
                  <input
                    type="password"
                    className="form-control"
                    placeholder="Enter secure password"
                    value={setupPass}
                    onChange={(e) => setSetupPass(e.target.value)}
                    required
                  />
                </div>

                <button
                  type="submit"
                  className="btn btn-primary"
                  style={{ width: '100%', marginTop: '6px' }}
                  disabled={setupLoading}
                >
                  <span>{setupLoading ? 'Initializing...' : 'Create First Administrator'}</span>
                </button>

                <button
                  type="button"
                  className="btn btn-ghost"
                  style={{ width: '100%', marginTop: '8px' }}
                  onClick={() => setShowSetup(false)}
                >
                  Cancel & Return to Login
                </button>
              </form>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
