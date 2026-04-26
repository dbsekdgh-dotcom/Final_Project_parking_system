import { FiHome, FiTruck, FiGrid, FiTrendingUp } from "react-icons/fi";
import { getSummary } from "../api/dashboardApi";
import { useEffect, useState } from "react";

const CARDS = [
    {
        key: 'householdCount',
        label: '전체 세대수',
        Icon: FiHome,
        color: 'blue',
        unit: '세대',
    },
    {
        key: 'vehicleCount',
        label: '등록된 차량',
        Icon: FiTruck,
        color: 'green',
        unit: '대',
    },
    {
        key: 'parkingSpaceCount',
        label: '주차 공간',
        Icon: FiGrid,
        color: 'orange',
        unit: '칸',
    },
    {
        key: 'totalRevenue',
        label: '주차요금 수익',
        Icon: FiTrendingUp,
        color: 'purple',
        isAmount: true,
    },
];

export default function DashSummaryCards() {
    const [data, setData] = useState(null);

    useEffect(() =>{
        getSummary().then(setData).catch(() => {});
    },[]);
    
     return (
      <div className="dash__stats">
        {CARDS.map(c => {
          const raw = data?.[c.key];

          // 데이터 없으면 '–', 금액이면 ₩ 포맷, 아니면 숫자 + 단위
          const display =
            raw == null ? '–'
            : c.isAmount ? `₩${raw.toLocaleString()}`
            : `${raw.toLocaleString()} ${c.unit}`;

          return (
            <div key={c.key} className="dash__stat-card">
              {/* 아이콘 박스 */}
              <div className={`dash__stat-icon dash__stat-icon--${c.color}`}>
                <c.Icon size={18} />
              </div>
              {/* 텍스트 */}
              <div className="dash__stat-body">
                <span className="dash__stat-label">{c.label}</span>
                <span className="dash__stat-value">{display}</span>
              </div>
            </div>
          );
        })}
      </div>
    );
}