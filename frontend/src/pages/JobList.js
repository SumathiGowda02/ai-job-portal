import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';

export default function JobList() {
  const [jobs, setJobs] = useState([]);
  const [keyword, setKeyword] = useState('');
  const [location, setLocation] = useState('');
  const [loading, setLoading] = useState(true);

  const fetchJobs = async (params = {}) => {
    setLoading(true);
    try {
      const { data } = await api.get('/jobs', { params });
      setJobs(data.content || []);
    } catch (err) {
      setJobs([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchJobs(); }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    fetchJobs({ keyword: keyword || undefined, location: location || undefined });
  };

  return (
    <div className="page">
      <h2>Browse Jobs</h2>
      <form onSubmit={handleSearch} className="search-bar">
        <input placeholder="Keyword (e.g. React, Java)" value={keyword} onChange={(e) => setKeyword(e.target.value)} />
        <input placeholder="Location" value={location} onChange={(e) => setLocation(e.target.value)} />
        <button type="submit">Search</button>
      </form>

      {loading && <p>Loading...</p>}
      {!loading && jobs.length === 0 && <p>No jobs found.</p>}

      <div className="job-grid">
        {jobs.map((job) => (
          <Link to={`/jobs/${job.id}`} key={job.id} className="job-card">
            <h3>{job.title}</h3>
            <p>{job.companyName} • {job.location}</p>
            <p className="job-type">{job.jobType}</p>
          </Link>
        ))}
      </div>
    </div>
  );
}
