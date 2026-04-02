import React from "react";
import "./DashBoard.css";

const parkingData = {
  floors: [
    { name: "B1층", type: "외부 차량", available: 12, total: 30 },
    { name: "B2층", type: "입주민/신규 전용", available: 16, total: 30 },
  ],
  myStatus: {
    totalVisits: 1250,
    fuelUsed: "87L",
    dDay: "D-18",
  },
  recentLogs: [
    { number: "59허 3724", type: "입차", time: "1분전" },
    { number: "59허 3724", type: "출차", time: "1분전" },
    { number: "34나 9012", type: "예약", time: "15분전" },
    { number: "59허 3724", type: "입차", time: "1시간전" },
  ],
};

const typeColor = {
  입차: "#4CAF50",
  출차: "#F44336",
  예약: "#FFC107",
};

const DashBoard = () => {
  return (
    <div className="dashboard">
      {/* 상단 요약 영역 */}
      <div className="floor-summary">
        {parkingData.floors.map((floor) => (
          <div key={floor.name} className="floor-card">
            <div className="floor-name">{floor.name}</div>
            <div className="floor-type">{floor.type}</div>
            <div className="floor-availability">
              <span className="available">{floor.available}</span> / {floor.total} 가용
            </div>
            <div className="progress-bar">
              <div
                className="progress"
                style={{ width: `${(floor.available / floor.total) * 100}%` }}
              ></div>
            </div>
          </div>
        ))}
      </div>

      {/* 빠른 메뉴 */}
      <div className="quick-menu">
        {["정기권", "방문 예약", "마이페이지", "민원신고"].map((item) => (
          <div key={item} className="menu-item">
            {item}
          </div>
        ))}
      </div>

      {/* 내 현황 */}
      <div className="my-status">
        <div className="status-item">
          <div className="status-label">총 방문 횟수</div>
          <div className="status-value">{parkingData.myStatus.totalVisits}</div>
        </div>
        <div className="status-item">
          <div className="status-label">연료 사용량</div>
          <div className="status-value">{parkingData.myStatus.fuelUsed}</div>
        </div>
        <div className="status-item">
          <div className="status-label">D-day</div>
          <div className="status-value">{parkingData.myStatus.dDay}</div>
        </div>
      </div>

      {/* 최근 입출차 내역 */}
      <div className="recent-logs">
        <h3>최근 입출차 내역</h3>
        {parkingData.recentLogs.map((log, idx) => (
          <div key={idx} className="log-item">
            <div className="car-number">{log.number}</div>
            <div
              className="log-type"
              style={{ backgroundColor: typeColor[log.type] }}
            >
              {log.type}
            </div>
            <div className="log-time">{log.time}</div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default DashBoard;