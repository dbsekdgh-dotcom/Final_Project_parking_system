import React, { useEffect, useState } from "react";
import axios from "axios";
import Swal from "sweetalert2";
import { useNavigate } from "react-router-dom";
import "./DashBoard.css";

const typeColor = {
  입차: "#4CAF50",
  출차: "#F44336",
  예약: "#FFC107",
};

const DashBoard = () => {
  //상태 관리 (데이터, 로딩)
  const[data,setData]= useState(null);
  const[loading, setLoading]= useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    //로그인 성공 메시지
    const name= sessionStorage.getItem("loginSuccess");
    if (name){
      sessionStorage.removeItem("loginSuccess");
      Swal.fire({
        icon: "success",
        title: "로그인 성공",
        confirmButtonText: "확인",
        confirmButtonColor: "#3085d6",
      });
    }

    //백엔드 데이터 호출
    const fetchDashboard = async () =>{
      try {
        const response = await axios.get("http://localhost:8081/api/dashboard?userId=69");
        setData(response.data);
      }catch (error){
        console.error("데이터 로드 실패:", error);
      }finally{
        setLoading(false);
      }
    };
 
    fetchDashboard();
  },[]);

  //메뉴클릭 시 페이지 이동 함수
    const handleMenuClick = (menu) => {
      if (menu === "정기권") navigate("/subscription");
      else if (menu === "방문 예약") navigate("/reservation"); 
      else if (menu === "마이페이지") navigate("/mypage");
      else if (menu === "민원신고") navigate("/report");
    };

  // 로딩 처리
    if (loading) return <div className="loading">데이터를 불러오는 중...</div>;
    if (!data) return <div className="error">데이터를 표시할 수 없습니다.</div>;

    return (
      <div className="dashboard">
        {/* 상단 요약 영역 */}
        <div className="floor-summary">
          {[data.b1Detail, data.b2Detail].map((floor) => (
            <div key={floor.floorName} className="floor-card">
              <div className="floor-name">{floor.floorName}</div>
              <div className="floor-type">{floor.description}</div>
              <div className="floor-availability">
                <span className="available">{floor.available}</span> / {floor.total} 
              </div>
              <div className="progress-bar">
                <div
                  className="progress"
                  style={{ width: `${floor.occupancyRate}%` }} //점유율반영
                ></div>
              </div>
            </div>
          ))}
        </div>

      {/* 빠른 메뉴 */}
      <div className="quick-menu">
        {["정기권", "방문 예약", "마이페이지", "민원신고"].map((item) => (
          <div key={item} className="menu-item" onClick={() =>handleMenuClick(item)} style={{cursor: 'pointer'}} >
            {item}
          </div>
        ))}
      </div>

    {/* 내 현황 섹션 */}
      <div className="my-status-container">
        <h3>내 현황</h3>
        <div className="status-grid">
          {/* 1. 포인트 */}
          <div className="status-card">
            <div className="status-icon">📈</div>
            <div className="status-value">{data.myPoint?.toLocaleString()}</div>
            <div className="status-label">포인트</div>
          </div>

          {/* 2. 중앙 차량 정보 */}
          <div className="status-card center">
            <div className="car-plate">{data.myCarNumber}</div>
            <div className="status-title">현재 주차 중</div>
            <div className="status-detail">
              {data.myCarLocation} · {data.parkingDuration}
            </div>
          </div>

          {/* 3. 정기권 */}
          <div className="status-card">
            <div className="status-icon">🛡️</div>
            <div className="status-value">D-{data.subscriptionDDay}</div>
            <div className="status-label">정기권 만료</div>
          </div>
        </div>
      </div>

        {/* 최근 입출차 내역 */}
        <div className="recent-logs">
          <div className="logs-header">
            <h3>최근 입출차 내역</h3>
            <span className="live-badge">● 실시간</span>
          </div>
        {data.recentLogs.map((log, idx) => (
          <div key={idx} className="log-item">
            {/* 왼쪽 섹션: 아이콘과 정보를 바짝 붙입니다 */}
            <div className="log-left-section">
              <div 
                className="log-icon" 
                style={{ backgroundColor: typeColor[log.status] || '#ccc' }}
              >
                {log.status === "입차" ? "입" : log.status === "출차" ? "출" : "예"}
              </div>
              
              <div className="log-info">
                <div className="log-car-number">{log.carNumber}</div>
                <div className="log-sub-info">
                  {log.location} / {log.timeAgo}
                </div>
              </div>
            </div>

            {/* 오른쪽 상태: 얘는 여전히 젤 우측 끝에! */}
            <div className="log-status-text" style={{ color: typeColor[log.status] }}>
              {log.status}
            </div>
          </div>
        ))}
        </div>
      </div>
    );
  };
 

export default DashBoard;