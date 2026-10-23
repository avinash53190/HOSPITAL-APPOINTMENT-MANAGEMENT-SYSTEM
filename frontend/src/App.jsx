import React, { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import Home from './pages/Home';
import Register from './pages/Register';
import Login from './pages/Login';
import PatientDashboard from './pages/PatientDashboard';
import DoctorList from './pages/DoctorList';
import BookAppointment from './pages/BookAppointment';
import MyAppointments from './pages/MyAppointments';
import AdminPortal from './pages/AdminPortal';
import { api } from './services/api';

export default function App() {
  const [auth, setAuth] = useState({
    authenticated: false,
    role: null,
    user: null
  });
  const [activeTab, setActiveTab] = useState('home');
  const [selectedDoctor, setSelectedDoctor] = useState(null);
  const [message, setMessage] = useState(null); // { type: 'success' | 'error', text: '' }

  // Check active session on initial load
  useEffect(() => {
    checkActiveSession();
  }, []);

  const checkActiveSession = async () => {
    try {
      const data = await api.checkSession();
      if (data && data.authenticated) {
        setAuth({
          authenticated: true,
          role: data.role,
          user: data.user
        });
      } else {
        setAuth({ authenticated: false, role: null, user: null });
      }
    } catch (err) {
      // Backend not yet reachable or no session
      setAuth({ authenticated: false, role: null, user: null });
    }
  };

  const showMessage = (type, text) => {
    setMessage({ type, text });
    setTimeout(() => {
      setMessage(null);
    }, 4500);
  };

  const handleLoginSuccess = (user, role) => {
    setAuth({
      authenticated: true,
      role: role,
      user: user
    });
    if (role === 'ADMIN') {
      setActiveTab('admin');
    } else {
      setActiveTab('dashboard');
    }
  };

  const handleLogout = async () => {
    try {
      await api.logout();
      setAuth({ authenticated: false, role: null, user: null });
      showMessage('success', 'Logged out successfully! Session invalidated on server.');
      setActiveTab('login');
    } catch (err) {
      showMessage('error', 'Logout error: ' + err.message);
    }
  };

  const renderContent = () => {
    switch (activeTab) {
      case 'home':
        return (
          <Home
            auth={auth}
            onNavigate={setActiveTab}
            onSelectDoctor={setSelectedDoctor}
          />
        );

      case 'doctors':
        return (
          <DoctorList
            auth={auth}
            onSelectDoctor={(doc) => setSelectedDoctor(doc)}
            onNavigate={setActiveTab}
          />
        );

      case 'register':
        return (
          <Register
            onNavigate={setActiveTab}
            onShowMessage={showMessage}
          />
        );

      case 'login':
        return (
          <Login
            onLoginSuccess={handleLoginSuccess}
            onNavigate={setActiveTab}
            onShowMessage={showMessage}
          />
        );

      case 'dashboard':
        return auth.authenticated && auth.role === 'PATIENT' ? (
          <PatientDashboard auth={auth} onNavigate={setActiveTab} />
        ) : (
          <Login onLoginSuccess={handleLoginSuccess} onNavigate={setActiveTab} onShowMessage={showMessage} />
        );

      case 'book':
        return auth.authenticated && auth.role === 'PATIENT' ? (
          <BookAppointment
            selectedDoctor={selectedDoctor}
            onNavigate={setActiveTab}
            onShowMessage={showMessage}
          />
        ) : (
          <Login onLoginSuccess={handleLoginSuccess} onNavigate={setActiveTab} onShowMessage={showMessage} />
        );

      case 'my-appointments':
        return auth.authenticated && auth.role === 'PATIENT' ? (
          <MyAppointments
            onNavigate={setActiveTab}
            onShowMessage={showMessage}
          />
        ) : (
          <Login onLoginSuccess={handleLoginSuccess} onNavigate={setActiveTab} onShowMessage={showMessage} />
        );

      case 'admin':
        return auth.authenticated && auth.role === 'ADMIN' ? (
          <AdminPortal onShowMessage={showMessage} />
        ) : (
          <Login onLoginSuccess={handleLoginSuccess} onNavigate={setActiveTab} onShowMessage={showMessage} />
        );

      default:
        return (
          <Home
            auth={auth}
            onNavigate={setActiveTab}
            onSelectDoctor={setSelectedDoctor}
          />
        );
    }
  };

  return (
    <>
      <Navbar
        auth={auth}
        onNavigate={setActiveTab}
        activeTab={activeTab}
        onLogout={handleLogout}
      />

      <main className="main-content">
        {/* Flash Notification Banner */}
        {message && (
          <div className={`alert alert-${message.type}`}>
            <span>{message.type === 'success' ? '✓ ' : '⚠️ '}{message.text}</span>
            <button
              onClick={() => setMessage(null)}
              style={{ background: 'transparent', border: 'none', cursor: 'pointer', fontWeight: 'bold' }}
            >
              ✕
            </button>
          </div>
        )}

        {renderContent()}
      </main>

      <footer>
        <p>
          <strong>CityCare Hospital Appointment Management System</strong> &bull; MCA First Semester Java & Web Programming Practical Project
        </p>
        <p style={{ marginTop: '5px', fontSize: '12px' }}>
          Technologies: Core Java &bull; Servlets &bull; JSP &bull; MySQL &bull; JDBC &bull; React.js &bull; Apache Tomcat
        </p>
      </footer>
    </>
  );
}
