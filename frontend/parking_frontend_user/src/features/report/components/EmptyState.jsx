import React from 'react';
import './EmptyState.css';

export default function EmptyState({ message = '내역이 없습니다.' }) {
  return (
    <div className="empty-state">
      <div className="empty-state__icon">📋</div>
      <p className="empty-state__text">{message}</p>
      <p className="empty-state__sub">접수된 신고 내역이 여기에 표시됩니다.</p>
    </div>
  );
}
