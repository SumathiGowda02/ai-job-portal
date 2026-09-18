import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import MatchScoreBadge from '../components/MatchScoreBadge';

export default function JobDetail() {
  const { id } = useParams();
  const [job, setJob] = useState(null);
  const [resumes, setResumes] = useState([]);
  const [selectedResume, setSelectedResume] = useState('');
  const [existingApplication, setExistingApplication] = useState(null);
  const [message, setMessage] = useState('');
  const [applying, setApplying] = useState(false);
  const { user } = useAuth();
  const navigate = useNavigate();

  const loadJob = () => api.get(`/jobs/${id}`).then((res) => setJob(res.data)).catch(() => setMessage('Failed to load this job.'));

  useEffect(() => { loadJob(); }, [id]);

  const loadCandidateData = () => {
    api.get('/resumes/my').then((res) => setResumes(res.data)).catch(() => {});
    api.get('/applications/my').then((res) => {
      const existing = res.data.find((a) => String(a.jobId) === String(id));
      setExistingApplication(existing || null);
    }).catch(() => {});
  };

  useEffect(() => {
    if (user?.role === 'CANDIDATE') loadCandidateData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user, id]);

  const handleApply = async () => {
    if (!selectedResume) {
      setMessage('Please select a resume first.');
      return;
    }

    if (applying) return;

    setApplying(true);
    setMessage('Applying... Please wait while we analyze your resume.');

    try {
      const { data } = await api.post('/applications', null, {
        params: { jobId: id, resumeId: selectedResume }
      });

    const matchType = data.matchSummary?.startsWith('Keyword-based match')
      ? 'Keyword match'
      : 'AI match';

    setMessage(`Applied! ${matchType}: ${data.matchScore}%`);

    loadCandidateData();
  } catch (err) {
    setMessage(err.response?.data?.message || 'Failed to apply');
  } finally {
    setApplying(false);
  }
};

  const handleWithdraw = async () => {
    if (!existingApplication) return;
    try {
      await api.delete(`/applications/${existingApplication.id}`);
      setMessage('Application withdrawn.');
      setExistingApplication(null);
    } catch (err) {
      setMessage(err.response?.data?.message || 'Failed to withdraw');
    }
  };

  if (!job) return <p className="page">Loading...</p>;

  return (
    <div className="page">
      <h2>{job.title}</h2>
      <p><strong>{job.companyName}</strong> — {job.location} — {job.jobType}</p>
      {(job.minSalary || job.maxSalary) && <p>Salary: ₹{job.minSalary} - ₹{job.maxSalary}</p>}
      <p>{job.description}</p>
      {job.requiredSkills?.length > 0 && (
        <p><strong>Required skills:</strong> {job.requiredSkills.join(', ')}</p>
      )}

      {!user && <button onClick={() => navigate('/login')}>Login to apply</button>}

      {user?.role === 'CANDIDATE' && existingApplication && (
        <div className="apply-box">
          <h4>You've already applied</h4>
          <p>Status: <strong>{existingApplication.status}</strong></p>
          {existingApplication.matchScore != null && <MatchScoreBadge
  score={existingApplication.matchScore}
  summary={existingApplication.matchSummary}
/>}
          {existingApplication.status === 'APPLIED' && (
            <button className="delete-btn" onClick={handleWithdraw}>Withdraw Application</button>
          )}
        </div>
      )}

      {user?.role === 'CANDIDATE' && !existingApplication && (
        <div className="apply-box">
          <h4>Apply with a resume</h4>
          {resumes.length === 0 ? (
            <p>You have no resumes uploaded. <Link to="/resumes">Upload one</Link>.</p>
          ) : (
            <>
              <select value={selectedResume} onChange={(e) => setSelectedResume(e.target.value)}>
                <option value="">Select a resume</option>
                {resumes.map((r) => (
                  <option key={r.id} value={r.id}>{r.originalFileName}</option>
                ))}
              </select>
              <button onClick={handleApply} disabled={applying}>
              {applying ? 'Applying...' : 'Apply Now'}
              </button>
            </>
          )}
        </div>
      )}
      {message && <p>{message}</p>}
    </div>
  );
}
