import React, { useEffect, useState } from 'react';
import './EntryParkingSpace.css';
import { useLocation, useNavigate } from 'react-router-dom';
import { fetchParkingLogType, fetchEntrySpace, confirmEnter, cancelEntry } from '../../api/EntryApi';

const FLOOR_BY_TYPE = {
  RESIDENT: 'B2',
  VISIT: 'B1',
  USER: 'B1',
  RESERVATION: 'B1',
};

const EntryParkingSpace = () => {
  const { state } = useLocation();
  const navigate = useNavigate();
  const parkingLogId = state?.parkingLogId;

  const [floor, setFloor] = useState(null);
  const [spotData, setSpotData] = useState([]);
  const [selectedSpotId, setSelectedSpotId] = useState(null);
  const [loading, setLoading] = useState(false);

  // 1) parkingTypeSnapshot 조회 → 층 결정
  useEffect(() => {
    if (!parkingLogId) return;
    fetchParkingLogType(parkingLogId)
      .then(({ parkingTypeSnapshot }) => {
        setFloor(FLOOR_BY_TYPE[parkingTypeSnapshot] ?? 'B1');
      })
      .catch((err) => console.error('주차 타입 조회 실패:', err));
  }, [parkingLogId]);

  // 2) 층이 결정되면 해당 층 자리 조회
  useEffect(() => {
    if (!floor) return;
    fetchEntrySpace(floor)
      .then(setSpotData)
      .catch((err) => console.error('자리 목록 조회 실패:', err));
  }, [floor]);

  const handleSpotClick = (spot) => {
    if (spot.status !== 'AVAILABLE') return;
    setSelectedSpotId(spot.id);
  };

  const getSpotClass = (spot) => {
    const classes = ['parking-spot'];
    if (spot.status === 'OCCUPIED') classes.push('occupied');
    else classes.push('available');
    if (spot.isDisabled) classes.push('disabled-spot');
    if (spot.isEvCharge) classes.push('ev-spot');
    if (selectedSpotId === spot.id) classes.push('selected');
    return classes.join(' ');
  };

  const getSubLabel = (spot) => {
    if (spot.isDisabled) return '장애인';
    if (spot.isEvCharge) return 'EV';
    return null;
  };

  // 입차 확정: 자리 배정 + DETECTED → ENTERED (단일 API 호출)
  const handleSubmit = async () => {
    if (!selectedSpotId) return;
    setLoading(true);
    try {
      await confirmEnter({ parkingLogId, spaceId: selectedSpotId });
      navigate('/entry-complete', { state: { parkingLogId } });
    } catch (err) {
      console.error('입차 확정 실패:', err);
      alert('선택한 자리를 배정할 수 없습니다. 다시 선택해주세요.');
      setSelectedSpotId(null);
    } finally {
      setLoading(false);
    }
  };

  // 회차: DETECTED → ENTRY_CANCELLED
  const handleCancel = async () => {
    if (parkingLogId) {
      try {
        await cancelEntry(parkingLogId);
      } catch (err) {
        console.error('회차 처리 실패:', err);
      }
    }
    navigate('/');
  };

  const selectedSpotCode = spotData.find((s) => s.id === selectedSpotId)?.spaceCode;

  return (
    <div className="parking-container">
      <header className="parking-header">
        <h1>주차 공간 선택</h1>
        {floor && <span className="floor-badge">{floor} 층</span>}
      </header>

      <div className="map-canvas">
        {spotData.map((spot) => (
          <div
            key={spot.id}
            className={getSpotClass(spot)}
            onClick={() => handleSpotClick(spot)}
          >
            <div className="spot-text-wrapper">
              <span className="spot-label">{spot.spaceCode}</span>
              {getSubLabel(spot) && (
                <span className="sub-label">{getSubLabel(spot)}</span>
              )}
            </div>
          </div>
        ))}
      </div>

      <footer className="parking-footer">
        <p>{selectedSpotCode ? `선택된 자리: ${selectedSpotCode}` : '공간을 선택해주세요'}</p>
        <button
          className="submit-btn"
          disabled={!selectedSpotId || loading}
          onClick={handleSubmit}
        >
          {loading ? '처리 중...' : '입차'}
        </button>
        <button
          className="cancel-btn"
          disabled={loading}
          onClick={handleCancel}
        >
          회차
        </button>
      </footer>
    </div>
  );
};

export default EntryParkingSpace;
