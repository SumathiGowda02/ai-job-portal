import React from 'react';
import { Link } from 'react-router-dom';

export default function Home() {
  return (
    <div className="home-page">

      {/* Hero Section */}
      <section className="home-hero">
        <div className="home-hero-content">

          <div className="home-badge">
            🤖 AI-Powered Recruitment
          </div>

          <h1>
            Find the Right Job.
            <br />
            <span>Match with Confidence.</span>
          </h1>

          <p>
            AI Job Portal connects candidates with opportunities and helps
            recruiters find the right talent using intelligent resume
            matching.
          </p>

          <div className="hero-actions">
            <Link to="/jobs" className="btn home-primary-btn">
              🔍 Browse Jobs
            </Link>

            <Link to="/register" className="btn btn-outline home-secondary-btn">
              Get Started →
            </Link>
          </div>

        </div>
      </section>


      {/* Features */}
      <section className="home-features">

        <div className="home-section-heading">
          <h2>Everything you need for your job search</h2>
          <p>
            A simple platform for candidates and recruiters.
          </p>
        </div>

        <div className="feature-grid">

          <div className="feature-card">
            <div className="feature-icon">🔍</div>
            <h3>Smart Job Search</h3>
            <p>
              Browse available jobs and find opportunities that match
              your skills and career goals.
            </p>
          </div>

          <div className="feature-card">
            <div className="feature-icon">🤖</div>
            <h3>AI Resume Matching</h3>
            <p>
              Get an AI-powered match score by comparing your resume
              with the required skills of a job.
            </p>
          </div>

          <div className="feature-card">
            <div className="feature-icon">📄</div>
            <h3>Resume Management</h3>
            <p>
              Upload and manage your resumes in one place and choose
              the right resume when applying.
            </p>
          </div>

        </div>
      </section>


      {/* How It Works */}
      <section className="how-it-works">

        <div className="home-section-heading">
          <h2>How it works</h2>
          <p>Get started in just a few simple steps.</p>
        </div>

        <div className="steps-grid">

          <div className="step-card">
            <div className="step-number">1</div>
            <h3>Create your profile</h3>
            <p>
              Register and add your professional information and skills.
            </p>
          </div>

          <div className="step-card">
            <div className="step-number">2</div>
            <h3>Upload your resume</h3>
            <p>
              Keep your resume ready and select it when applying for jobs.
            </p>
          </div>

          <div className="step-card">
            <div className="step-number">3</div>
            <h3>Apply for jobs</h3>
            <p>
              Apply to suitable jobs and receive an AI-based match score.
            </p>
          </div>

        </div>
      </section>


      {/* Final CTA */}
      <section className="home-cta">

        <h2>Ready to find your next opportunity?</h2>

        <p>
          Explore jobs and take the next step in your career.
        </p>

        <Link to="/jobs" className="btn home-primary-btn">
          Explore Jobs →
        </Link>

      </section>

    </div>
  );
}