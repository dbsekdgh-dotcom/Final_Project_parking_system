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

const QUICK_MENUS = [
  {
    label: "정기권",
    icon: (
      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round">
        <path d="M2 9a3 3 0 0 1 0 6v2a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-2a3 3 0 0 1 0-6V7a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2Z" />
        <path d="M13 5v2" /><path d="M13 17v2" /><path d="M13 11v2" />
      </svg>
    ),
  },
  {
    label: "방문 예약",
    icon: (
      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round">
        <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
        <line x1="16" y1="2" x2="16" y2="6" /><line x1="8" y1="2" x2="8" y2="6" /><line x1="3" y1="10" x2="21" y2="10" />
      </svg>
    ),
  },
  {
    label: "마이페이지",
    icon: (
      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round">
        <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2" />
        <circle cx="12" cy="7" r="4" />
      </svg>
    ),
  },
  {
    label: "민원신고",
    icon: (
      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round">
        <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z" />
        <line x1="12" y1="9" x2="12" y2="13" /><line x1="12" y1="17" x2="12.01" y2="17" />
      </svg>
    ),
  },
];

const CACHE_KEY = "dashboardCache_p0";

const loadingToast = Swal.mixin({
  toast: true,
  position: "top-end",
  showConfirmButton: false,
  timer: 2000,
  timerProgressBar: true,
});

const DashBoard = () => {
  const cached = sessionStorage.getItem(CACHE_KEY);
  const[data,setData]= useState(cached ? JSON.parse(cached) : null);
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

    //캐시 없을 때만 토스트 표시 (로그인 성공 모달과 충돌 방지)
    const isFirstLoad = page === 0 && !sessionStorage.getItem(CACHE_KEY);
    if (isFirstLoad && !name) {
      loadingToast.fire({ icon: "info", title: "데이터를 불러오는 중..." });
    }

    const fetchDashboard = async () => {
      try {
        const response = await api.get(`/api/user/dashboard?page=${page}`);
        setData(response.data);
        if (page === 0) sessionStorage.setItem(CACHE_KEY, JSON.stringify(response.data));
      } catch (error) {
        console.error("데이터 로드 실패:", error);
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

  if (!data) return null;

    return (
      <div className="dashboard">
        <h2 className="dashboard__title">대시보드</h2>
        {/* 상단 요약 영역 */}
        <div className="floor-summary">
          {[data.b1Detail, data.b2Detail].filter(Boolean).map((floor) => (
            <div key={floor.floorName} className="floor-card">
              <div className="floor-name">{floor.floorName}</div>
              <div className="floor-type">{floor.description}</div>
              <div className="floor-availability">
                <span className="available">{floor.total - floor.available}</span> / {floor.total}
              </div>
              <div className="progress-bar">
                <div
                  className="progress"
                  style={{
                    width: `${floor.occupancyRate}%`,
                    background: floor.occupancyRate >= 80 ? "#f44336"
                              : floor.occupancyRate >= 50 ? "#ff9800"
                              : "#4caf50",
                  }}
                ></div>
              </div>
            </div>
          ))}
        </div>

      {/* 빠른 메뉴 */}
      <div className="quick-menu">
        {QUICK_MENUS.map(({ label, icon }) => (
          <div key={label} className="menu-item" onClick={() => handleMenuClick(label)}>
            <span className="menu-item__icon">{icon}</span>
            <span className="menu-item__label">{label}</span>
          </div>
        ))}
      </div>

    {/* 내 현황 섹션 */}
      <div className="my-status-container">
        <h3>내 현황</h3>
        <div className="status-grid">
          {/* 1. 포인트 */}
          <div className="dash-status-card">
            <div className="status-icon">📈</div>
            <div className="status-value">{data.myPoint?.toLocaleString()}</div>
            <div className="status-label">포인트</div>
          </div>

          {/* 2. 중앙 차량 정보 */}
          <div className="status-card center">
            <div className="car-plate">{data.myCarNumber}</div>
            <div className="status-title">
              {data.myCarLocation !== "주차 정보 없음" ? "현재 주차 중" : "현재 미주차"}
            </div>
            <div className="status-detail">
              {data.myCarLocation} · {data.parkingDuration}
            </div>
          </div>

          {/* 3. 정기권 */}
          <div className="dash-status-card">
            <div className="status-icon">🛡️</div>
            <div className="status-value">
              {data.subscriptionDDay != null ? `D-${data.subscriptionDDay}` : "없음"}
            </div>
            <div className="status-label">
              {data.subscriptionDDay != null ? "정기권 만료" : "정기권 없음"}
            </div>
          </div>
        </div>
      </div>

          {/* 최근 입출차 내역 */}
          <div className="recent-logs">
            <div className="logs-header">
              <h3>최근 입출차 내역</h3>
            </div>

            {data.recentLogs && data.recentLogs.content && data.recentLogs.content.length > 0 ? (
              data.recentLogs.content.map((log) => {
                const config = getStatusConfig(log.status);
                return (
                  <div key={log.parkingLogId || log.createdAt} className="log-item">
                    <div className="log-left-section">
                      <div className="log-icon" style={{ backgroundColor: config.color }}>
                        {config.label}
                      </div>
                      <div className="log-info">
                        <div className="log-car-number">{log.carNumber || "번호 없음"}</div>
                        <div className="log-sub-info">
                          {log.message || log.location} / {log.createdAt ? new Date(log.createdAt).toLocaleString() : "시간 정보 없음"}
                        </div>
                      </div>
                    </div>
                    <div className="log-status-text" style={{ color: typeColor[log.status] }}>
                      {log.status}
                    </div>
                  </div>
                );
              })
            ) : (
              <div className="dash-no-data">
                <div className="dash-no-data__icon">🚗</div>
                <p className="dash-no-data__text">입출차 내역이 없습니다.</p>
                <p className="dash-no-data__sub">입출차 기록이 여기에 표시됩니다.</p>
              </div>
            )}

          {data.recentLogs && data.recentLogs.totalPages > 1 && (
            <div className="dash-pagination">
              <button
                className="dash-page-btn"
                onClick={() => setPage(prev => Math.max(0, prev - 1))}
                disabled={data.recentLogs.first}
              >이전</button>

              {Array.from({ length: data.recentLogs.totalPages }, (_, i) => (
                <button
                  key={i}
                  className={`dash-page-btn${i === page ? " dash-page-btn--active" : ""}`}
                  onClick={() => setPage(i)}
                >{i + 1}</button>
              ))}

              <button
                className="dash-page-btn"
                onClick={() => setPage(prev => Math.min(data.recentLogs.totalPages - 1, prev + 1))}
                disabled={data.recentLogs.last}
              >다음</button>
            </div>
          )}
        </div>
      </div>
    );
  };
 

export default DashBoard;