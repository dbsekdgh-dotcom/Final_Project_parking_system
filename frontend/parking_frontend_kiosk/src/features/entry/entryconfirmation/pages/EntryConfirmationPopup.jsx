import React from 'react';
import './EntryConfirmationPopup.css';
import { useNavigate } from 'react-router-dom';

const EntryConfirmationPopup = ({ cameras = [] }) => {
  const navigate = useNavigate();

  // 카메라 데이터 중 ENTRY 타입만 필터링
  const entryCameras = cameras.filter(cam => cam.camera_type === 'ENTRY');

  const handleEntryClick = (camera) => {
    // 입차 로직 수행 후 페이지 이동 (필요 시 camera_id 전달)
    console.log(`Selected Camera ID: ${camera.camera_id}`);
    navigate("/entry-parkingspace", { state: { cameraId: camera.camera_id } });
  };

  return (
    <div className="popup-overlay">
      <div className="popup-content">
        {/* 중앙 체크 아이콘 */}
        <div className="icon-wrapper">
          <svg viewBox="0 0 52 52" className="checkmark-icon" xmlns="http://www.w3.org/2000/svg">
            <circle cx="26" cy="26" r="25" fill="none" stroke="currentColor" strokeWidth="2"/>
            <path fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" d="M14 27l8 8 16-16"/>
          </svg>
        </div>

        {/* 문구 영역 */}
        <h2 className="popup-title">입차를 진행하시겠습니까?</h2>
        <p className="popup-desc">입구 버튼을 누르면 입차가 기록됩니다</p>

        {/* 중앙 장식선 */}
        <div className="divider-line"></div>

        {/* 입구 버튼 영역 (동적 생성) */}
        <div className="entry-buttons-container">
          {entryCameras.map((cam) => (
            <button 
              key={cam.camera_id} 
              className="btn-entry"
              onClick={() => handleEntryClick(cam)}
            >
              {cam.description || `${cam.camera_id}번 입구`}
            </button>
          ))}
        </div>

        {/* 회차 버튼 영역 */}
        <div className="cancel-button-container">
          <button className="btn-return" onClick={() => navigate("/")}>
            회차
          </button>
        </div>
      </div>
    </div>
  );
};

export default EntryConfirmationPopup;