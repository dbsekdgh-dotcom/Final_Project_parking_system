  import { useEffect, useState } from 'react';
  import {
    BarChart, Bar,
    XAxis, YAxis,
    CartesianGrid, Tooltip,
    ResponsiveContainer,
  } from 'recharts';
  import { getRevenue } from '../api/dashboardApi';
  
  //매출 현황 탭 목록
  // id : getRevenue(type)에 넘기는 값, 백앤드 파라미터와 일치
  const TABS = [
    { id: 'TOTAL', label: '전체' },
    { id: 'TICKET', label: '할인권' },
    { id: 'PARKING', label: '주차' },
    { id: 'SUBSCRIPTION', label: '정기권' },
  ];

  // DarkTooltip
  // recharts가 마우스 호버시 자동으로 호출하는 커스텀 툴팁
  // Props (recharts가 자동 주입):
  // action {boolean} - 현제 호버 중인지 여부
  // payload {Array} - 호버된 바의 데이터 [{ value: 금액}]
  // label {string} - x축 값 (예: "1월")
  function DarkTooltip({ active, payload, label}){
    if (!active || !payload?.length) return null;

    return(
        <div style={{
        background: '#2a2a2a',
        border: '1px solid #3f3f3f',
        borderRadius: 8,
        padding: '10px 14px',
      }}>
         <p style={{ color: '#9ca3af', fontSize: 12, margin: '0 0 4px' }}>{label}</p>
          <p style={{ color: '#f9fafb', fontSize: 14, fontWeight: 600, margin: 0 }}>
             ₩{payload[0].value?.toLocaleString()}
          </p>
        </div>
    )
  }
  //salesChart
  // props:
  // activeTab {string} - 현재 선택된 탭 id
  // onTabChange {fn} - 탭 클릭 시 DashBoard.jsx
  // 탭 바뀔 때마다 getRevenue(activeTab) 재호출
  export default function SalesChart({ activeTab, onTabChange }){
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        setLoading(true);
        getRevenue(activeTab)
            .then(setData)
            .catch(() => setData(null))
            .finally(() => setLoading(false));
    }, [activeTab]);

    // monsthly가 없으면 빈 배열로 대체
    // [{ month: '1월', amount: 2000000}, ...]
    const monthly = data?.monthly ?? [];
    
     return (
      <div className="dash__panel">
        {/* 패널 헤더: 제목 + 현재 탭 기준 합계 */}
        <div className="dash__panel-header">
          <span className="dash__panel-title">순매출 현황</span>
          {/* totalAmount가 있을 때만 합계 표시, 없으면 숨김 */}
          {data?.totalAmount != null && (
            <span className="dash__panel-sub">
              합계: ₩{data.totalAmount.toLocaleString()}
            </span>
          )}
        </div>

        {/* 탭 바 */}
        <div className="dash__tabs">
          {TABS.map(t => (
            <button
              key={t.id}
              // 현재 탭이면 active 클래스 추가 (파란 언더라인)
              className={`dash__tab${activeTab === t.id ? ' dash__tab--active' : ''}`}
              // 클릭 시 부모(DashBoard.jsx)의 setSalesTab 호출
              // → SalesDetailTable도 같은 activeTab을 받으므로 함께 갱신됨
              onClick={() => onTabChange(t.id)}
            >
              {t.label}
            </button>
          ))}
        </div>

        {/* 차트 영역: 고정 높이 220px */}
        <div style={{ height: 220 }}>
          {loading ? (
            <div className="dash__empty">불러오는 중...</div>
          ) : monthly.length === 0 ? (
            <div className="dash__empty">데이터 없음</div>
          ) : (
            // ResponsiveContainer: 부모 너비에 맞게 차트 자동 리사이즈
            <ResponsiveContainer width="100%" height="100%">
              <BarChart
                data={monthly} // [{ month, amount }] 배열
                margin={{ top: 4, right: 4, left: 0, bottom: 0 }}
              >
                {/* 가로 격자선만 표시, 다크 색상 */}
                <CartesianGrid strokeDasharray="3 3" stroke="#2e2e2e" vertical={false} />

                {/* X축: month 필드 사용, 눈금선/축선 숨김 */}
                <XAxis
                  dataKey="month"
                  tick={{ fill: '#9ca3af', fontSize: 12 }}
                  axisLine={false}
                  tickLine={false}
                />

                {/* Y축: 큰 숫자는 K/M으로 축약 표시 */}
                <YAxis
                  tick={{ fill: '#9ca3af', fontSize: 11 }}
                  axisLine={false}
                  tickLine={false}
                  tickFormatter={v =>
                    v >= 1000000 ? `${(v / 1000000).toFixed(1)}M`
                    : v >= 1000  ? `${(v / 1000).toFixed(0)}K`
                    : v
                  }
                />

                {/* 커스텀 툴팁 */}
                <Tooltip
                  content={<DarkTooltip />}
                  cursor={{ fill: 'rgba(255,255,255,0.04)' }} // 호버 시 바 배경
                />

                {/* 실제 바: amount 필드, 상단 모서리 둥글게 */}
                <Bar dataKey="amount" fill="#2563eb" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>
    );
  }