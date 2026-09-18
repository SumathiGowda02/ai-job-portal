import React from 'react';

export default function MatchScoreBadge({ score, summary }) {
  const color = score >= 75 ? '#2ecc71' : score >= 50 ? '#f1c40f' : '#e74c3c';

  const isKeywordMatch = summary?.startsWith('Keyword-based match');
  const label = isKeywordMatch ? 'Keyword Match' : 'AI Match';

  return (
    <span
      style={{
        background: color,
        color: '#fff',
        padding: '2px 10px',
        borderRadius: 12,
        fontSize: 13,
        fontWeight: 'bold'
      }}
    >
      {label}: {score}%
    </span>
  );
}