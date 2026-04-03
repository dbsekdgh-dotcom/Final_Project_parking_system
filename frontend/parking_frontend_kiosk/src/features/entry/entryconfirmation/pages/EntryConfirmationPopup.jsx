import React from 'react';
import './EntryConfirmationPopup.css';
import { useNavigate } from 'react-router-dom';

const EntryConfirmationPopup = ({ onConfirm, onCancel }) => {
   const navigate=useNavigate();
  return (
    <div className="popup-overlay">
      <div className="popup-content">
        {/* 중앙 체크 아이콘 (SVG) */}
        <div className="icon-wrapper">
          <svg viewBox="0 0 52 52" className="checkmark-icon" xmlns="http://www.w3.org/2000/svg">
            <circle cx="26" cy="26" r="25" fill="none" stroke="currentColor" strokeWidth="2"/>
            <path fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" d="M14 27l8 8 16-16"/>
          </svg>
        </div>

        {/* 메인 제목 문구 */}
        <h2 className="popup-title">입차를 진행하시겠습니까?</h2>

        {/* 서브 설명 문구 */}
        <p className="popup-desc">입차 버튼을 누르면 입차가 기록됩니다</p>

        {/* 중앙 장식선 */}
        <div className="divider-line"></div>

        {/* 버튼 영역 */}
        <div className="popup-buttons">
          <button className="btn btn-primary" onClick={()=>navigate("/entry-parkingspace")}>
            입차
          </button>
          <button className="btn btn-secondary" onClick={()=>navigate("/")}>
            회차
          </button>
        </div>
      </div>
    </div>
  );
};

export default EntryConfirmationPopup;