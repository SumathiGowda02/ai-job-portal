import React, { useEffect, useState } from 'react';
import api from '../services/api';

const API_ORIGIN = (process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api').replace(/\/api\/?$/, '');

function resolveFileUrl(s3Url) {
  if (!s3Url) return '#';
  return s3Url.startsWith('http') ? s3Url : `${API_ORIGIN}${s3Url}`;
}

export default function Resumes() {
  const [resumes, setResumes] = useState([]);
  const [file, setFile] = useState(null);
  const [message, setMessage] = useState('');

  const loadResumes = () => api.get('/resumes/my').then((res) => setResumes(res.data)).catch((err) => setMessage(err.response?.data?.message || 'Failed to load resumes'));

  useEffect(() => { loadResumes(); }, []);

  const handleUpload = async (e) => {
    e.preventDefault();
    if (!file) return;
    const formData = new FormData();
    formData.append('file', file);
    try {
      await api.post('/resumes/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } });
      setMessage('Resume uploaded successfully.');
      setFile(null);
      loadResumes();
    } catch (err) {
      setMessage(err.response?.data?.message || 'Upload failed');
    }
  };

  const handleDelete = async (id) => {
    setMessage('');
    try {
      await api.delete(`/resumes/${id}`);
      loadResumes();
    } catch (err) {
      setMessage(err.response?.data?.message || 'Failed to delete resume');
    }
  };

  return (
    <div className="page">
      <h2>My Resumes</h2>
      <form onSubmit={handleUpload} className="upload-form">
        <input type="file" accept=".pdf" onChange={(e) => setFile(e.target.files[0])} />
        <button type="submit">Upload PDF Resume</button>
      </form>
      {message && <p>{message}</p>}

      <ul className="resume-list">
        {resumes.map((r) => (
          <li key={r.id} className="resume-item">
            <span>
              <a href={resolveFileUrl(r.s3Url)} target="_blank" rel="noreferrer">{r.originalFileName}</a>
              {' '}— uploaded {new Date(r.uploadedAt).toLocaleDateString()}
            </span>
            <button className="delete-btn" onClick={() => handleDelete(r.id)}>Delete</button>
          </li>
        ))}
      </ul>
    </div>
  );
}
