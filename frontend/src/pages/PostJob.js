import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import api from '../services/api';

export default function PostJob() {
  const { id } = useParams();
  const isEditMode = Boolean(id);
  const [form, setForm] = useState({
    title: '', description: '', location: '', jobType: 'FULL_TIME',
    requiredSkills: '', minSalary: '', maxSalary: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(isEditMode);
  const navigate = useNavigate();

  useEffect(() => {
    if (isEditMode) {
      api.get(`/jobs/${id}`).then((res) => {
        const job = res.data;
        setForm({
          title: job.title || '',
          description: job.description || '',
          location: job.location || '',
          jobType: job.jobType || 'FULL_TIME',
          requiredSkills: (job.requiredSkills || []).join(', '),
          minSalary: job.minSalary ?? '',
          maxSalary: job.maxSalary ?? '',
        });
        setLoading(false);
      });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    const payload = {
      ...form,
      requiredSkills: form.requiredSkills.split(',').map((s) => s.trim()).filter(Boolean),
      minSalary: form.minSalary ? Number(form.minSalary) : null,
      maxSalary: form.maxSalary ? Number(form.maxSalary) : null,
    };
    try {
      if (isEditMode) {
        await api.put(`/jobs/${id}`, payload);
      } else {
        await api.post('/jobs', payload);
      }
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || `Failed to ${isEditMode ? 'update' : 'post'} job`);
    }
  };

  if (loading) return <p className="page">Loading...</p>;

  return (
    <div className="page form-container">
      <h2>{isEditMode ? 'Edit Job' : 'Post a Job'}</h2>
      {error && <p className="error">{error}</p>}
      <form onSubmit={handleSubmit}>
        <input name="title" placeholder="Job title" value={form.title} onChange={handleChange} required />
        <textarea name="description" placeholder="Job description" rows={6} value={form.description} onChange={handleChange} required />
        <input name="location" placeholder="Location" value={form.location} onChange={handleChange} required />
        <select name="jobType" value={form.jobType} onChange={handleChange}>
          <option value="FULL_TIME">Full-time</option>
          <option value="PART_TIME">Part-time</option>
          <option value="INTERNSHIP">Internship</option>
          <option value="CONTRACT">Contract</option>
          <option value="REMOTE">Remote</option>
        </select>
        <input name="requiredSkills" placeholder="Required skills (comma-separated)" value={form.requiredSkills} onChange={handleChange} />
        <input name="minSalary" type="number" placeholder="Min salary" value={form.minSalary} onChange={handleChange} />
        <input name="maxSalary" type="number" placeholder="Max salary" value={form.maxSalary} onChange={handleChange} />
        <button type="submit">{isEditMode ? 'Save Changes' : 'Post Job'}</button>
      </form>
    </div>
  );
}
