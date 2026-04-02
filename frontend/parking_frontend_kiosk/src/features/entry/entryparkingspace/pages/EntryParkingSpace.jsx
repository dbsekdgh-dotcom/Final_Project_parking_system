import React, { useState } from 'react';
import './EntryParkingSpace.css';

const EntryParkingSpace = () => {
  // 데이터 구조: label(코드), subLabel(명칭) 분리
  const spotData = [
    // A행 (A1~A9 일반, A10 장애인)
    { id: 'A1', label: 'A1' }, { id: 'A2', label: 'A2' }, { id: 'A3', label: 'A3' },
    { id: 'A4', label: 'A4' }, { id: 'A5', label: 'A5' }, { id: 'A6', label: 'A6' },
    { id: 'A7', label: 'A7' }, { id: 'A8', label: 'A8' }, { id: 'A9', label: 'A9' },
    { id: 'A10', label: 'A10', subLabel: '장애인', typeClass: 'disabled-spot' },

    // B행 (B1~B9 일반, B10 전기차)
    { id: 'B1', label: 'B1' }, { id: 'B2', label: 'B2' }, { id: 'B3', label: 'B3' },
    { id: 'B4', label: 'B4' }, { id: 'B5', label: 'B5' }, { id: 'B6', label: 'B6' },
    { id: 'B7', label: 'B7' }, { id: 'B8', label: 'B8' }, { id: 'B9', label: 'B9' },
    { id: 'B10', label: 'B10', subLabel: '전기차', typeClass: 'ev-spot' },

    // C행 (C1~C9 일반, C10 전기차)
    { id: 'C1', label: 'C1' }, { id: 'C2', label: 'C2' }, { id: 'C3', label: 'C3' },
    { id: 'C4', label: 'C4' }, { id: 'C5', label: 'C5' }, { id: 'C6', label: 'C6' },
    { id: 'C7', label: 'C7' }, { id: 'C8', label: 'C8' }, { id: 'C9', label: 'C9' },
    { id: 'C10', label: 'C10', subLabel: '전기차', typeClass: 'ev-spot' },
  ];

  const [selectedFloor, setSelectedFloor] = useState('B1');
  const [selectedSpot, setSelectedSpot] = useState(null);
  const [showDropdown, setShowDropdown] = useState(false);

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