import React from 'react';
import './EntryComplatePage.css';

const EntryCompletePage = ({ onGoHome }) => {
  return (
    <div className="entry-complete-wrapper">
      <div className="entry-complete-content">
        {/* 중앙 체크 아이콘 (SVG) */}
        <div className="icon-wrapper">
          <svg viewBox="0 0 52 52" className="checkmark-icon" xmlns="http://www.w3.org/2000/svg">
            <circle cx="26" cy="26" r="25" fill="none" stroke="currentColor" strokeWidth="2"/>
            <path fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" d="M14 27l8 8 16-16"/>
          </svg>
        </div>

        {/* 메인 완료 문구 */}
        <h1 className="entry-title">입차가 완료되었습니다</h1>

        {/* 서브 설명 문구 */}
        <p className="entry-desc">안전한 주차를 이용해 주세요</p>

        {/* 중앙 장식선 */}
        <div className="divider-line"></div>

        {/* 홈으로 버튼 */}
        <div className="button-area">
          <button className="go-home-btn" onClick={onGoHome}>
            홈으로 돌아가기
          </button>
        </div>
      </div>
    </div>
  );
};

export default EntryCompletePage;