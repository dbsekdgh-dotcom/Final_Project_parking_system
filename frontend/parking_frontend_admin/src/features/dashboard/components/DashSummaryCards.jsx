import { FiHome, FiTruck, FiGrid, FiTrendingUp } from "react-icons/fi";
import { getSummary } from "../api/dashboardApi";
import { useEffect, useState } from "react";

const CARDS = [
    {
        key: 'householdCount',
        totalKey: 'totalHouseholdCount',
        label: '입주 세대수',
        Icon: FiHome,
        color: 'blue',
        unit: '세대',
        isFraction: true,
    },
    {
        key: 'vehicleCount',
        label: '등록된 차량',
        Icon: FiTruck,
        color: 'green',
        unit: '대',
    },
    {
        key: 'occupiedParkingSpaceCount',
        totalKey: 'parkingSpaceCount',
        label: '주차 공간',
        Icon: FiGrid,
        color: 'orange',
        unit: '칸',
        isFraction: true,
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
          const total = data?.[c.totalKey];

          const display =
            raw == null ? '–'
            : c.isAmount ? `₩${raw.toLocaleString()}`
            : c.isFraction ? `${raw.toLocaleString()} / ${total?.toLocaleString() ?? '?'} ${c.unit}`
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