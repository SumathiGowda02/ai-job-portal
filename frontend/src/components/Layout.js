import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Footer from './Footer';

export default function Layout({ children }) {
  const { user, logout } = useAuth();
  const [menuOpen, setMenuOpen] = useState(false);

  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    setMenuOpen(false);
    logout();
    navigate('/login');
  };

  const closeMenu = () => {
    setMenuOpen(false);
  };

  /*
   * Close the menu automatically whenever
   * the user changes to another page.
   */
  useEffect(() => {
    setMenuOpen(false);
  }, [location.pathname]);

  const candidateLinks = [
    {
      to: '/dashboard',
      label: 'Dashboard',
      icon: '🏠'
    },
    {
      to: '/jobs',
      label: 'Browse Jobs',
      icon: '🔍'
    },
    {
      to: '/resumes',
      label: 'My Resumes',
      icon: '📄'
    },
    {
      to: '/profile',
      label: 'Profile',
      icon: '👤'
    }
  ];

  const recruiterLinks = [
    {
      to: '/dashboard',
      label: 'Dashboard',
      icon: '🏠'
    },
    {
      to: '/post-job',
      label: 'Post a Job',
      icon: '➕'
    },
    {
      to: '/jobs',
      label: 'Browse Jobs',
      icon: '🔍'
    },
    {
      to: '/profile',
      label: 'Profile',
      icon: '👤'
    }
  ];

  const navigationLinks = user
    ? user.role === 'RECRUITER'
      ? recruiterLinks
      : candidateLinks
    : [];

  return (
    <div className="app-shell">

      {/* =========================
          TOP NAVIGATION BAR
         ========================= */}
      <header className="topbar">

        <div className="topbar-left">

          {/* Hamburger button */}
          {user && (
            <button
              type="button"
              className="hamburger-button"
              onClick={() => setMenuOpen(true)}
              aria-label="Open navigation menu"
            >
              <span></span>
              <span></span>
              <span></span>
            </button>
          )}

          {/* Website name */}
          <Link
            to="/"
            className="topbar-brand"
            onClick={closeMenu}
          >
            AI Job Portal
          </Link>

        </div>

        {/* Account area */}
        <div className="profile-menu">

          {user ? (
            <button
              type="button"
              className="profile-trigger"
              onClick={() => navigate('/profile')}
            >
              <span className="profile-avatar">
                {user.fullName
                  ? user.fullName.charAt(0).toUpperCase()
                  : 'U'}
              </span>

              <span className="profile-name">
                {user.fullName || 'Account'}
              </span>

              <span className="caret">▾</span>
            </button>
          ) : (
            <div className="guest-actions">
              <Link to="/login">Sign In</Link>
              <Link to="/register">Register</Link>
            </div>
          )}

        </div>
      </header>


      {/* =========================
          DARK OVERLAY
         ========================= */}
      {menuOpen && (
        <div
          className="menu-overlay"
          onClick={closeMenu}
          aria-hidden="true"
        />
      )}


      {/* =========================
          HAMBURGER SIDE MENU
         ========================= */}
      {user && (
        <aside
          className={`navigation-drawer ${
            menuOpen ? 'navigation-drawer-open' : ''
          }`}
        >

          {/* Drawer header */}
          <div className="drawer-header">

            <Link
              to="/"
              className="drawer-brand"
              onClick={closeMenu}
            >
              AI Job Portal
            </Link>

            <button
              type="button"
              className="drawer-close"
              onClick={closeMenu}
              aria-label="Close navigation menu"
            >
              ✕
            </button>

          </div>


          {/* User information */}
          <div className="drawer-user">

            <div className="drawer-avatar">
              {user.fullName
                ? user.fullName.charAt(0).toUpperCase()
                : 'U'}
            </div>

            <div>
              <strong>
                {user.fullName || 'User'}
              </strong>

              <small>
                {user.role === 'RECRUITER'
                  ? 'Recruiter'
                  : 'Candidate'}
              </small>
            </div>

          </div>


          {/* Navigation */}
          <nav className="drawer-navigation">

            {navigationLinks.map((link) => {

              const isActive =
                link.to === '/jobs'
                  ? location.pathname === '/jobs' ||
                    location.pathname.startsWith('/jobs/')
                  : location.pathname === link.to;

              return (
                <Link
                  key={link.to}
                  to={link.to}
                  onClick={closeMenu}
                  className={`drawer-link ${
                    isActive ? 'drawer-link-active' : ''
                  }`}
                >
                  <span className="drawer-icon">
                    {link.icon}
                  </span>

                  <span>
                    {link.label}
                  </span>
                </Link>
              );
            })}

          </nav>


          {/* Drawer bottom */}
          <div className="drawer-footer">

            <button
              type="button"
              className="drawer-logout"
              onClick={handleLogout}
            >
              <span className="drawer-icon">🚪</span>
              <span>Logout</span>
            </button>

          </div>

        </aside>
      )}


      {/* =========================
          PAGE CONTENT
         ========================= */}
      <main className="app-content">
        {children}
      </main>


      {/* =========================
          FOOTER
         ========================= */}
      <Footer />

    </div>
  );
}