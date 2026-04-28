import React, { useEffect, useState } from "react";
import api from "../../auth/api/axios";
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
  const[page, setPage] = useState(0);
  const navigate = useNavigate();
  
  const getStatusConfig = (status) => {
  if (!status) return { label: "기", color: "#ccc" };
  
  // "입차완료", "입차" 등 '입차'라는 글자가 포함된 경우
  if (status.includes("입차")) {
    return { label: "입", color: "#4CAF50" }; // 초록색
  }
  // "출차완료", "출차", "강제출차" 등 '출차'라는 글자가 포함된 경우
  if (status.includes("출차")) {
    return { label: "출", color: "#F44336" }; // 빨간색
  }
  
  return { label: "기", color: "#9E9E9E" }; // 기타 상태 (회색)
};

  useEffect(() => {
    //로그인 성공 메시지
    const name = sessionStorage.getItem("loginSuccess");

    if(name){
      sessionStorage.removeItem("loginSuccess");
      Swal.fire({
        icon: "success",
        title: "로그인 성공",
        text: `환영합니다, ${name}님!`,
        confirmButtonText: "확인",
        confirmButtonColor: "#3085d6",
      });
    }

    //백엔드 데이터 호출
    const fetchDashboard = async () =>{
       const currentUserId = localStorage.getItem("userId");

        if(!currentUserId){
          console.error("세션에 userId가 없네요");
          setLoading(false);
          return;
        }

        try{
        const response = await api.get(
          `/api/dashboard?userId=${currentUserId}&page=${page}`);
        setData(response.data);

      }catch (error){
        console.error("데이터 로드 실패:", error);
      }finally{
        setLoading(false);
      }
    };
 
    fetchDashboard();
  },[page]);

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

            {data.recentLogs && data.recentLogs.content && data.recentLogs.content.length > 0 ? (
              data.recentLogs.content.map((log) => {
              const config = getStatusConfig(log.status);

                return (
                  <div key={log.parkingLogId || log.createdAt} className="log-item">
                    <div className="log-left-section">
                      {/* 왼쪽 동그라미 아이콘 */}
                      <div
                        className="log-icon"
                        style={{ backgroundColor: config.color }}
                      >
                        {config.label}
                      </div>

                      <div className="log-info">
                        <div className="log-car-number">{log.carNumber || "번호 없음"}</div>
                        <div className="log-sub-info">
                          {/* location 대신 message, timeAgo 대신 createdAt 사용 */}
                          {log.message || log.location} / {log.createdAt ? new Date(log.createdAt).toLocaleString() : "시간 정보 없음"}
                        </div>
                      </div>
                    </div>

                    <div
                      className="log-status-text"
                      style={{ color: typeColor[log.status] }}
                    >
                      {log.status}
                    </div>
                  </div>
                );
              })
            ) : (
              /* 데이터가 없을 때 표시될 문구 */
              <div className="no-data" style={{ textAlign: 'center', padding: '20px', color: '#999' }}>
                표시할 입출차 내역이 없습니다. (총 {data.recentLogs?.totalElements || 0}건)
              </div>
            )}

          <div className="pagination-controlls" style={{display: 'flex',justifyContent: 'center', gap: '20px', marginTop: '20px'}}>
            <button onClick={() => setPage(prev =>Math.max(0, prev -1 ))}
            disabled={data.recentLogs.first} style={{cursor: data.recentLogs.first ? 'default' : 'pointer'}}>
              이전
            </button>

            <span style={{ fontWeight: 'bold'}}>
              {data.recentLogs.totalElements === 0 ?0 : page +1} / {data.recentLogs.totalPages || 0}
            </span>

            <button onClick={()=> setPage(page +1)}
            disabled={data.recentLogs.last} style={{cursor: data.recentLogs.last ? 'default' : 'pointer'}}>
              다음
            </button>
          </div>
        </div>
      </div>
    );
  };
 

export default DashBoard;