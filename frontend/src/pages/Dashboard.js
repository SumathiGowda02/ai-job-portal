import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import MatchScoreBadge from '../components/MatchScoreBadge';

const API_ORIGIN = (
  process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api'
).replace(/\/api\/?$/, '');

function resolveFileUrl(url) {
  if (!url) return '#';
  return url.startsWith('http') ? url : `${API_ORIGIN}${url}`;
}

export default function Dashboard() {
  const { user } = useAuth();

  const [myJobs, setMyJobs] = useState([]);
  const [applicants, setApplicants] = useState({});
  const [myApplications, setMyApplications] = useState([]);
  const [message, setMessage] = useState('');

  useEffect(() => {
    if (user.role === 'RECRUITER') {
      api.get('/jobs/my')
        .then((res) => setMyJobs(res.data.content || []))
        .catch((err) =>
          setMessage(
            err.response?.data?.message ||
            'Failed to load your job postings'
          )
        );
    } else {
      loadMyApplications();
    }

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user]);

  const loadMyApplications = () =>
    api.get('/applications/my')
      .then((res) => setMyApplications(res.data))
      .catch((err) =>
        setMessage(
          err.response?.data?.message ||
          'Failed to load your applications'
        )
      );

  const loadApplicants = async (jobId) => {
    try {
      const { data } = await api.get(`/applications/job/${jobId}`);
      setApplicants((prev) => ({
        ...prev,
        [jobId]: data
      }));
    } catch (err) {
      setMessage(
        err.response?.data?.message ||
        'Failed to load applicants'
      );
    }
  };

  const updateStatus = async (appId, jobId, status) => {
    setMessage('');

    try {
      await api.patch(
        `/applications/${appId}/status`,
        null,
        { params: { status } }
      );

      loadApplicants(jobId);
    } catch (err) {
      setMessage(
        err.response?.data?.message ||
        'Failed to update status'
      );
    }
  };

  const deleteJob = async (jobId) => {
    if (!window.confirm('Delete this job posting?')) return;

    try {
      await api.delete(`/jobs/${jobId}`);

      setMyJobs((prev) =>
        prev.filter((j) => j.id !== jobId)
      );
    } catch (err) {
      setMessage(
        err.response?.data?.message ||
        'Failed to delete job'
      );
    }
  };

  const statusClass = (status) => {
    if (status === 'ACCEPTED' || status === 'HIRED') {
      return 'status-accepted';
    }

    if (status === 'REJECTED') {
      return 'status-rejected';
    }

    return 'status-pending';
  };

  if (user.role === 'RECRUITER') {
    return (
      <div className="page">
        <h2>My Job Postings</h2>

        {message && <p>{message}</p>}

        {myJobs.length === 0 && (
          <p>
            You haven't posted any jobs yet.{' '}
            <Link to="/post-job">Post one</Link>.
          </p>
        )}

        {myJobs.map((job) => (
          <div key={job.id} className="dashboard-job">

            <div className="dashboard-job-header">
              <h3>
                {job.title} — {job.location}
              </h3>

              <div className="job-actions">
                <Link
                  to={`/edit-job/${job.id}`}
                  className="btn-small"
                >
                  Edit
                </Link>

                <button
                  className="delete-btn"
                  onClick={() => deleteJob(job.id)}
                >
                  Delete
                </button>
              </div>
            </div>

            <button onClick={() => loadApplicants(job.id)}>
              View Applicants (ranked by AI match)
            </button>

            {applicants[job.id] && (
              applicants[job.id].length === 0 ? (
                <p>No applicants yet.</p>
              ) : (
                <table className="applicant-table">
                  <thead>
                    <tr>
                      <th>Candidate</th>
                      <th>Match</th>
                      <th>Resume</th>
                      <th>Status</th>
                      <th>Action</th>
                    </tr>
                  </thead>

                  <tbody>
                    {applicants[job.id].map((app) => (
                      <tr key={app.id}>

                        <td>
                          {app.candidateName}
                        </td>

                        <td>
                          <MatchScoreBadge
                            score={app.matchScore ?? 0}
                            summary={app.matchSummary}
                          />

                          <div className="summary">
                            {app.matchSummary}
                          </div>

                          {app.matchedSkills?.length > 0 && (
                            <div className="skills-section">
                              <strong>
                                Matched Skills:
                              </strong>

                              <div>
                                {app.matchedSkills.join(', ')}
                              </div>
                            </div>
                          )}

                          {app.missingSkills?.length > 0 && (
                            <div className="skills-section">
                              <strong>
                                Missing Skills:
                              </strong>

                              <div>
                                {app.missingSkills.join(', ')}
                              </div>
                            </div>
                          )}
                        </td>

                        <td>
                          <a
                            href={resolveFileUrl(app.resumeUrl)}
                            target="_blank"
                            rel="noreferrer"
                          >
                            {app.resumeFileName}
                          </a>
                        </td>

                        <td>
                          <span
                            className={`status-badge ${statusClass(
                              app.status
                            )}`}
                          >
                            {app.status}
                          </span>
                        </td>

                        <td className="action-buttons">

                          {app.status !== 'ACCEPTED' && (
                            <button
                              className="accept-btn"
                              onClick={() =>
                                updateStatus(
                                  app.id,
                                  job.id,
                                  'ACCEPTED'
                                )
                              }
                            >
                              Accept
                            </button>
                          )}

                          {app.status !== 'REJECTED' && (
                            <button
                              className="reject-btn"
                              onClick={() =>
                                updateStatus(
                                  app.id,
                                  job.id,
                                  'REJECTED'
                                )
                              }
                            >
                              Reject
                            </button>
                          )}

                        </td>

                      </tr>
                    ))}
                  </tbody>
                </table>
              )
            )}
          </div>
        ))}
      </div>
    );
  }

  return (
    <div className="page">

      <h2>My Applications</h2>

      {message && <p>{message}</p>}

      {myApplications.length === 0 && (
        <p>
          You haven't applied to any jobs yet.{' '}
          <Link to="/jobs">Browse jobs</Link>.
        </p>
      )}

      <table className="applicant-table">
        <thead>
          <tr>
            <th>Job</th>
            <th>Match</th>
            <th>Status</th>
            <th>Applied</th>
          </tr>
        </thead>

        <tbody>
          {myApplications.map((app) => (
            <tr key={app.id}>

              <td>
                <Link to={`/jobs/${app.jobId}`}>
                  {app.jobTitle}
                </Link>
              </td>

              <td>
                <MatchScoreBadge
                  score={app.matchScore ?? 0}
                  summary={app.matchSummary}
                />

                <div className="summary">
                  {app.matchSummary}
                </div>
              </td>

              <td>
                <span
                  className={`status-badge ${statusClass(
                    app.status
                  )}`}
                >
                  {app.status}
                </span>
              </td>

              <td>
                {new Date(app.appliedAt).toLocaleDateString()}
              </td>

            </tr>
          ))}
        </tbody>
      </table>

    </div>
  );
}