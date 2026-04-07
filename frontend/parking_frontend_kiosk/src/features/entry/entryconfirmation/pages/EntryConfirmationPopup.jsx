import React, { useEffect, useState } from 'react';
import './EntryConfirmationPopup.css';
import { useNavigate, useLocation } from 'react-router-dom';
import { fetchEntryCameras, confirmEnter } from '../../../entry/api/EntryApi';

const EntryConfirmationPopup = () => {
  const navigate = useNavigate();
  const { state } = useLocation();
  const parkingLogId = state?.parkingLogId;

  const [cameras, setCameras] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchEntryCameras()
      .then(setCameras)
      .catch((err) => console.error('카메라 목록 조회 실패:', err));
  }, []);

  const handleEntryClick = async (camera) => {
    if (!parkingLogId) {
      alert('입차 정보가 없습니다. 다시 시도해주세요.');
      navigate('/entry-exit');
      return;
    }
    setLoading(true);
    try {
      await confirmEnter({ parkingLogId, cameraId: camera.cameraId });
      navigate('/entry-parkingspace', { state: { cameraId: camera.cameraId } });
    } catch (error) {
      console.error('입차 확정 실패:', error);
      alert('입차 처리에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="popup-overlay">
      <div className="popup-content">
        <div className="icon-wrapper">
          <svg viewBox="0 0 52 52" className="checkmark-icon" xmlns="http://www.w3.org/2000/svg">
            <circle cx="26" cy="26" r="25" fill="none" stroke="currentColor" strokeWidth="2"/>
            <path fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" d="M14 27l8 8 16-16"/>
          </svg>
        </div>

        <h2 className="popup-title">입차를 진행하시겠습니까?</h2>
        <p className="popup-desc">입구 버튼을 누르면 입차가 기록됩니다</p>

        <div className="divider-line"></div>

        <div className="entry-buttons-container">
          {cameras.map((cam) => (
            <button
              key={cam.cameraId}
              className="btn-entry"
              onClick={() => handleEntryClick(cam)}
              disabled={loading}
            >
              {cam.description || cam.location || `${cam.cameraId}번 입구`}
            </button>
          ))}
        </div>

        <div className="cancel-button-container">
          <button className="btn-return" onClick={() => navigate('/')}>
            회차
          </button>
        </div>
      </div>
    </div>
  );
};

export default EntryConfirmationPopup;
