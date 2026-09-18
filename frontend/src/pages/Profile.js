import React, { useEffect, useState } from 'react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';

export default function Profile() {
  const { user } = useAuth();
  const [profile, setProfile] = useState(null);
  const [form, setForm] = useState({
    fullName: '', companyName: '', companyDescription: '', education: '', skills: '', bio: '',
  });
  const [message, setMessage] = useState('');
  const [editing, setEditing] = useState(false);

  useEffect(() => {
    api.get('/profile/me').then((res) => {
      setProfile(res.data);
      setForm({
        fullName: res.data.fullName || '',
        companyName: res.data.companyName || '',
        companyDescription: res.data.companyDescription || '',
        education: res.data.education || '',
        skills: (res.data.skills || []).join(', '),
        bio: res.data.bio || '',
      });
    }).catch((err) => {
      setMessage(err.response?.data?.message || 'Failed to load profile — please try logging in again.');
    });
  }, []);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSave = async (e) => {
    e.preventDefault();
    setMessage('');
    try {
      const { data } = await api.put('/profile/me', {
        ...form,
        skills: form.skills.split(',').map((s) => s.trim()).filter(Boolean),
      });
      setProfile(data);
      setEditing(false);
      setMessage('Profile updated.');
    } catch (err) {
      setMessage(err.response?.data?.message || 'Failed to update profile');
    }
  };

  if (!profile) return <p className="page">{message || 'Loading...'}</p>;

  const isRecruiter = profile.role === 'RECRUITER';

  return (
    <div className="page">
      <h2>My Profile</h2>
      {message && <p>{message}</p>}

      {!editing ? (
        <div className="profile-card">
          <p><strong>Name:</strong> {profile.fullName}</p>
          <p><strong>Email:</strong> {profile.email}</p>
          <p><strong>Role:</strong> {profile.role}</p>

          {isRecruiter ? (
            <>
              <p><strong>Company:</strong> {profile.companyName || '—'}</p>
              <p><strong>About the company:</strong> {profile.companyDescription || '—'}</p>
            </>
          ) : (
            <>
              <p><strong>Education:</strong> {profile.education || '—'}</p>
              <p><strong>Skills:</strong> {(profile.skills || []).join(', ') || '—'}</p>
            </>
          )}
          <p><strong>Bio:</strong> {profile.bio || '—'}</p>

          <button onClick={() => setEditing(true)}>Edit Profile</button>
        </div>
      ) : (
        <form onSubmit={handleSave} className="form-container profile-form">
          <input name="fullName" placeholder="Full name" value={form.fullName} onChange={handleChange} />

          {isRecruiter ? (
            <>
              <input name="companyName" placeholder="Company name" value={form.companyName} onChange={handleChange} />
              <textarea name="companyDescription" placeholder="About your company" rows={4}
                value={form.companyDescription} onChange={handleChange} />
            </>
          ) : (
            <>
              <input name="education" placeholder="Education (e.g. B.Tech CSE, XYZ College, 2026)" value={form.education} onChange={handleChange} />
              <input name="skills" placeholder="Skills (comma-separated)" value={form.skills} onChange={handleChange} />
            </>
          )}

          <textarea name="bio" placeholder="Short bio" rows={3} value={form.bio} onChange={handleChange} />

          <div className="form-actions">
            <button type="submit">Save</button>
            <button type="button" className="btn-outline-btn" onClick={() => setEditing(false)}>Cancel</button>
          </div>
        </form>
      )}
    </div>
  );
}
