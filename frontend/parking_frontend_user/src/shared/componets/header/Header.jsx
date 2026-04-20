import { useEffect, useState } from 'react'
import './header-css.css'
import axios from 'axios';

export function Header() {
  const [parkingData, setParkingData] = useState({
    totalSpaces: 0,
    occupiedSpace: 0,
    availableSpaces: 0,
    occupancyRate: 0,
    targetFloor: '-'
  });

  useEffect(() =>{
    const fetchSummary = async () =>{
      try{
        //test용
        const userType = 'RESIDENT';
        const response = await axios.get(`http://localhost:8081/api/user/space/summary?userType=${userType}`);

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
        <span className="dashboard-header__eyebrow">실시간 현황</span>
        <h1 className="dashboard-header__title">Smart Parking</h1>
      </div>

      <div className="dashboard-header__stats">
        {/* 가용 주차면 구역 */}
        <div className="dashboard-header__stat">
          <span className="dashboard-header__stat-value">
            {parkingData.availableSpaces} / {parkingData.totalSpaces}
          </span>
          <span className="dashboard-header__stat-label">가용 주차면</span>
        </div>

        {/* 현재 점유율 구역 */}
        <div className="dashboard-header__stat">
          <span className="dashboard-header__stat-value">
            {parkingData.occupancyRate}%
          </span>
          <span className="dashboard-header__stat-label">현재 점유율</span>
        </div>

        {/* 추천 주차 층 구역 */}
        <div className="dashboard-header__stat">
          <span className="dashboard-header__stat-value dashboard-header__stat-value--accent">
            {parkingData.targetFloor}
          </span>
          <span className="dashboard-header__stat-label">내 주차 층</span>
        </div>
      </div>
    </header>
  )
}
