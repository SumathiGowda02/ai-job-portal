import React from 'react';

export default function Footer() {
  return (
    <footer className="site-footer">
      <div className="footer-inner">
        <div className="footer-col">
          <h4>AI Job Portal</h4>
          <p>
            Connecting candidates and recruiters with AI-powered resume matching —
            built as a full-stack portfolio project (Spring Boot, React, MySQL).
          </p>
        </div>
        <div className="footer-col">
          <h4>For Candidates</h4>
          <a href="/jobs">Browse Jobs</a>
          <a href="/register">Create an Account</a>
        </div>
        <div className="footer-col">
          <h4>For Recruiters</h4>
          <a href="/register">Post a Job</a>
          <a href="/login">Recruiter Login</a>
        </div>
        <div className="footer-col">
          <h4>About</h4>
          <p>Every application is scored by an LLM against the job description, so recruiters see best-fit candidates first.</p>
        </div>
      </div>
      <div className="footer-bottom">
        © {new Date().getFullYear()} AI Job Portal. Built for demonstration purposes.
      </div>
    </footer>
  );
}
