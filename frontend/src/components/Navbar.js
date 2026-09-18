import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav style={styles.nav}>
      <Link to="/" style={styles.brand}>AI Job Portal</Link>
      <div style={styles.links}>
        <Link to="/jobs" style={styles.link}>Browse Jobs</Link>
        {user && user.role === 'RECRUITER' && (
          <>
            <Link to="/post-job" style={styles.link}>Post a Job</Link>
            <Link to="/dashboard" style={styles.link}>My Postings</Link>
          </>
        )}
        {user && user.role === 'CANDIDATE' && (
          <>
            <Link to="/resumes" style={styles.link}>My Resumes</Link>
            <Link to="/dashboard" style={styles.link}>My Applications</Link>
          </>
        )}
        {!user && <Link to="/login" style={styles.link}>Login</Link>}
        {!user && <Link to="/register" style={styles.link}>Register</Link>}
        {user && <button onClick={handleLogout} style={styles.logoutBtn}>Logout ({user.fullName})</button>}
      </div>
    </nav>
  );
}

const styles = {
  nav: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '14px 28px', background: '#1a1a2e', color: '#fff' },
  brand: { color: '#fff', fontWeight: 'bold', fontSize: 20, textDecoration: 'none' },
  links: { display: 'flex', gap: 18, alignItems: 'center' },
  link: { color: '#e5e5e5', textDecoration: 'none' },
  logoutBtn: { background: '#e94560', color: '#fff', border: 'none', padding: '6px 12px', borderRadius: 4, cursor: 'pointer' },
};
