/**
 * API Service for communicating with the Java Servlet Backend.
 * Standard Fetch API with { credentials: 'include' } ensures
 * Tomcat's JSESSIONID cookie is attached to every request.
 */

const API_BASE = 'http://localhost:8080/api';

async function fetchJSON(url, options = {}) {
  const config = {
    ...options,
    credentials: 'include', // Ensures HttpSession JSESSIONID cookie is transmitted
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      ...(options.headers || {})
    }
  };

  const response = await fetch(url, config);
  const data = await response.json().catch(() => ({}));

  if (!response.ok) {
    const errorMsg = data.message || `Request failed with status ${response.status}`;
    throw new Error(errorMsg);
  }

  return data;
}

export const api = {
  // Check active HttpSession
  checkSession: () => fetchJSON(`${API_BASE}/auth/me`),

  // User & Admin Login
  login: (email, password, role = 'PATIENT') =>
    fetchJSON(`${API_BASE}/login`, {
      method: 'POST',
      body: JSON.stringify({ email, password, role })
    }),

  // First-Time Administrator Setup
  setupAdmin: (username, password) =>
    fetchJSON(`${API_BASE}/admin/setup`, {
      method: 'POST',
      body: JSON.stringify({ username, password })
    }),

  // Patient Registration
  register: (name, email, phone, password) =>
    fetchJSON(`${API_BASE}/register`, {
      method: 'POST',
      body: JSON.stringify({ name, email, phone, password })
    }),

  // Session Logout
  logout: () =>
    fetchJSON(`${API_BASE}/logout`, {
      method: 'POST'
    }),

  // Doctors
  getDoctors: (search = '', specialization = 'ALL') => {
    const params = new URLSearchParams();
    if (search) params.append('search', search);
    if (specialization && specialization !== 'ALL') params.append('specialization', specialization);
    const queryString = params.toString() ? `?${params.toString()}` : '';
    return fetchJSON(`${API_BASE}/doctors${queryString}`);
  },

  addDoctor: (name, specialization, available = true) =>
    fetchJSON(`${API_BASE}/doctors`, {
      method: 'POST',
      body: JSON.stringify({ name, specialization, available })
    }),

  // Appointments
  getAppointments: () => fetchJSON(`${API_BASE}/appointments`),

  bookAppointment: (doctorId, appointmentDate, appointmentTime) =>
    fetchJSON(`${API_BASE}/appointments`, {
      method: 'POST',
      body: JSON.stringify({ doctorId, appointmentDate, appointmentTime })
    }),

  cancelAppointment: (appointmentId) =>
    fetchJSON(`${API_BASE}/appointments/cancel`, {
      method: 'POST',
      body: JSON.stringify({ appointmentId })
    })
};

