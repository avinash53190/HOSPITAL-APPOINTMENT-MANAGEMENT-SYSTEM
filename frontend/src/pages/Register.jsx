import React, { useState } from 'react';
import {
  UserRound,
  Mail,
  Phone,
  Lock,
  Eye,
  EyeOff,
  CheckCircle2,
  ArrowRight
} from 'lucide-react';
import { api } from '../services/api';

export default function Register({ onNavigate, onShowMessage }) {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    // Client-side validations
    if (!formData.name.trim()) {
      setError('Full Name is required.');
      return;
    }
    if (!formData.email.trim() || !formData.email.includes('@')) {
      setError('Please provide a valid email address.');
      return;
    }
    if (!formData.phone.trim() || formData.phone.length < 10) {
      setError('Phone number must have at least 10 digits.');
      return;
    }
    if (formData.password.length < 6) {
      setError('Password must be at least 6 characters long.');
      return;
    }
    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    setLoading(true);
    try {
      const res = await api.register(
        formData.name,
        formData.email,
        formData.phone,
        formData.password
      );
      onShowMessage('success', res.message || 'Registration successful! Please login.');
      onNavigate('login');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page-container register-page">
      <div className="auth-card-split register-card">
        <div className="auth-visual-panel register-visual-panel">
          <div className="auth-visual-top">
            <span className="register-eyebrow">YOUR HEALTH, SIMPLIFIED</span>
            <h2>Care that starts with you.</h2>
            <p>
              Create your CityCare account and take the next step toward
              easier, more connected healthcare.
            </p>
          </div>

          <ul className="auth-features-list">
            <li className="auth-feature-item">
              <CheckCircle2 size={16} className="auth-feature-check" />
              <span>Book appointments in a few clicks</span>
            </li>
            <li className="auth-feature-item">
              <CheckCircle2 size={16} className="auth-feature-check" />
              <span>Find trusted doctors near you</span>
            </li>
            <li className="auth-feature-item">
              <CheckCircle2 size={16} className="auth-feature-check" />
              <span>Keep your appointments organized</span>
            </li>
          </ul>

          <div className="register-visual-note">Secure patient access by CityCare</div>
        </div>

        <div className="auth-form-panel register-form-panel">
          <div className="auth-header">
            <h2>Create your account</h2>
            <p>Join CityCare to book and manage your hospital appointments.</p>
          </div>

          {error && (
            <div className="register-error" role="alert">
              <span>⚠️ {error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <div className="form-field">
              <label className="form-label-clean" htmlFor="register-name">Full Name</label>
              <div className="input-with-icon">
                <UserRound size={16} className="input-icon-left" />
                <input
                  id="register-name"
                  type="text"
                  name="name"
                  className="input-clean"
                  placeholder="e.g. Ramesh Kumar"
                  value={formData.name}
                  onChange={handleChange}
                  autoComplete="name"
                  required
                />
              </div>
            </div>

            <div className="register-form-row">
              <div className="form-field">
                <label className="form-label-clean" htmlFor="register-email">Email Address</label>
                <div className="input-with-icon">
                  <Mail size={16} className="input-icon-left" />
                  <input
                    id="register-email"
                    type="email"
                    name="email"
                    className="input-clean"
                    placeholder="you@example.com"
                    value={formData.email}
                    onChange={handleChange}
                    autoComplete="email"
                    required
                  />
                </div>
              </div>

              <div className="form-field">
                <label className="form-label-clean" htmlFor="register-phone">Phone Number</label>
                <div className="input-with-icon">
                  <Phone size={16} className="input-icon-left" />
                  <input
                    id="register-phone"
                    type="tel"
                    name="phone"
                    className="input-clean"
                    placeholder="9876543210"
                    value={formData.phone}
                    onChange={handleChange}
                    autoComplete="tel"
                    required
                  />
                </div>
              </div>
            </div>

            <div className="register-form-row">
              <div className="form-field">
                <label className="form-label-clean" htmlFor="register-password">Password</label>
                <div className="input-with-icon">
                  <Lock size={16} className="input-icon-left" />
                  <input
                    id="register-password"
                    type={showPassword ? 'text' : 'password'}
                    name="password"
                    className="input-clean input-clean-with-toggle"
                    placeholder="6+ characters"
                    value={formData.password}
                    onChange={handleChange}
                    autoComplete="new-password"
                    required
                  />
                  <button
                    type="button"
                    className="input-password-toggle"
                    onClick={() => setShowPassword(!showPassword)}
                    aria-label={showPassword ? 'Hide password' : 'Show password'}
                  >
                    {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                  </button>
                </div>
              </div>

              <div className="form-field">
                <label className="form-label-clean" htmlFor="register-confirm-password">Confirm Password</label>
                <div className="input-with-icon">
                  <Lock size={16} className="input-icon-left" />
                  <input
                    id="register-confirm-password"
                    type={showConfirmPassword ? 'text' : 'password'}
                    name="confirmPassword"
                    className="input-clean input-clean-with-toggle"
                    placeholder="Repeat password"
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    autoComplete="new-password"
                    required
                  />
                  <button
                    type="button"
                    className="input-password-toggle"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    aria-label={showConfirmPassword ? 'Hide confirmation password' : 'Show confirmation password'}
                  >
                    {showConfirmPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                  </button>
                </div>
              </div>
            </div>

            <button type="submit" className="btn btn-primary register-submit" disabled={loading}>
              <span>{loading ? 'Creating Account...' : 'Create Patient Account'}</span>
              {!loading && <ArrowRight size={15} />}
            </button>
          </form>

          <div className="register-login-prompt">
            Already registered?{' '}
            <button type="button" onClick={() => onNavigate('login')}>
              Sign in here
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
