import { useEffect, useState } from 'react'
import './header-css.css'
import api from '../../../features/auth/api/axios';

function CarIcon() {
  return (
    <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round" className="dashboard-header__car-icon">
      <path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2" />
      <circle cx="7" cy="17" r="2" />
      <path d="M9 17h6" />
      <circle cx="17" cy="17" r="2" />
    </svg>
  )
}

export function Header() {
  const [parkingData, setParkingData] = useState({
    totalSpaces: 0,
    occupiedSpaces: 0,
    availableSpaces: 0,
    occupancyRate: 0,
    targetFloor: '-'
  });

  useEffect(() =>{
    const fetchSummary = async () =>{
      try{
        const userType = localStorage.getItem('userStatus') === 'RESIDENT' ? 'RESIDENT' : 'GENERAL';
        const response = await api.get(`/api/user/space/summary?userType=${userType}`);

        if (response.data){
          setParkingData(response.data);
        }
      }catch (error){
        console.error("데이터 로딩 실패:", error);
      }
    };

    fetchSummary();
  },[]);

  return (
    <header className="dashboard-header">
      <div className="dashboard-header__intro">
        <CarIcon />
        <h1 className="dashboard-header__title">Smart Parking</h1>
      </div>

      <div className="dashboard-header__stats">
        {/* 가용 주차면 구역 */}
        <div className="dashboard-header__stat">
          <span className="dashboard-header__stat-value">
            {parkingData.availableSpaces} / {parkingData.totalSpaces}
          </span>
          <span className="dashboard-header__stat-label">사용 가능 자리</span>
        </div>

        {/* 현재 점유율 구역 */}
        <div className="dashboard-header__stat">
          <span className="dashboard-header__stat-value">
            {parkingData.occupancyRate}%
          </span>
          <span className="dashboard-header__stat-label">현재 점유율</span>
        </div>

        {/* 주차 가능 층 구역 */}
        <div className="dashboard-header__stat">
          <span className="dashboard-header__stat-value dashboard-header__stat-value--accent">
            {parkingData.targetFloor || '-'}
          </span>
          <span className="dashboard-header__stat-label">주차 가능 층</span>
        </div>
      </div>
    </header>
  )
}
