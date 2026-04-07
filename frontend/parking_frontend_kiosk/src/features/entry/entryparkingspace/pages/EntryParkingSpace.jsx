import React, { useEffect, useState } from 'react';
import './EntryParkingSpace.css';
import { fetchEntrySpace } from '../../api/EntryApi';

const EntryParkingSpace = () => {
  // 데이터 구조: label(코드), subLabel(명칭) 분리
  const [spotData,setSpotData] = useState([]);

  const [selectedFloor, setSelectedFloor] = useState('B1');
  const [selectedSpot, setSelectedSpot] = useState(null);
  const [showDropdown, setShowDropdown] = useState(false);
  useEffect(()=>{
    fetchEntrySpace()
    .then(setSpotData)
    .catch((err)=>console.error('자리 목록 조회 실패:',err))
  },[]);
  
  return (
    <div className="parking-container">
      <header className="parking-header">
        <h1>입차 / 출차</h1>
        <div className={`floor-select-box ${showDropdown ? 'active' : ''}`} onClick={() => setShowDropdown(!showDropdown)}>
          <span className="selected-floor">{selectedFloor} 층</span>
          
          {showDropdown && (
            <ul className="floor-options">
              <li onClick={() => { setSelectedFloor('B1'); setShowDropdown(false); }}>B1 층</li>
              <li onClick={() => { setSelectedFloor('B2'); setShowDropdown(false); }}>B2 층</li>
            </ul>
          )}
        </div>
      </header>

      <div className="map-canvas">
        {spotData.map((spot) => (
          <div
            key={spot.id}
            className={`parking-spot ${spot.typeClass || ''} ${selectedSpot === spot.id ? 'selected' : ''}`}
            onClick={() => setSelectedSpot(spot.id)}
          >
            <div className="spot-text-wrapper">
              <span className="spot-label">{spot.label}</span>
              {/* subLabel이 있을 때만 줄바꿈과 함께 출력 */}
              {spot.subLabel && <span className="sub-label">{spot.subLabel}</span>}
            </div>
          </div>
        ))}
      </div>

      <footer className="parking-footer">
        <p>공간을 선택해주세요</p>
        <button className="submit-btn" disabled={!selectedSpot}>선택</button>
      </footer>
    </div>
  );
};

export default EntryParkingSpace;