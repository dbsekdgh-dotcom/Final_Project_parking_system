  import { useEffect, useState } from 'react';
  import {
    LineChart, Line,
    XAxis, YAxis,
    CartesianGrid, Tooltip,
    ResponsiveContainer,
  } from 'recharts';
  import { getUsage } from '../api/dashboardApi';

   const TABS = [
    { id: 'PARKING',      label: '주차장'   }, // 초기값
    { id: 'TICKET',       label: '할인권'   },
    { id: 'SUBSCRIPTION', label: '정기권'   },
    { id: 'RESERVATION',  label: '방문예약' },
    { id: 'POINT',        label: '포인트'   },
  ];

  function DarkTooltip({ active, payload, label }) {
    // active가 false이거나 payload가 비어있으면 툴팁 숨김
    if (!active || !payload?.length) return null;

    return (
      <div style={{
        background: '#2a2a2a',
        border: '1px solid #3f3f3f',
        borderRadius: 8,
        padding: '10px 14px',
      }}>
        {/* X축 레이블 (월) */}
        <p style={{ color: '#9ca3af', fontSize: 12, margin: '0 0 4px' }}>{label}</p>
        {/* 해당 월 사용량 - payload[0].value가 라인 차트의 count 값 */}
        <p style={{ color: '#f9fafb', fontSize: 14, fontWeight: 600, margin: 0 }}>
          {payload[0].value?.toLocaleString()}건
        </p>
      </div>
    );
  }
  
 export default function UsageChart({ activeTab, onTabChange }) {
    // API 응답 전체 저장
    // 형태: { totalCount, changePercent, monthly: [{ month, count }] }
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
      // activeTab이 바뀔 때마다 실행
      setLoading(true);
      getUsage(activeTab)
        .then(setData)
        .catch(() => setData(null)) // 에러 시 빈 차트 유지
        .finally(() => setLoading(false));
    }, [activeTab]); // activeTab이 의존성 → 탭 바뀔 때만 재호출

    // monthly가 없으면 빈 배열로 대체 (차트 에러 방지)
    // 형태: [{ month: '1월', count: 2500 }, ...]
    const monthly = data?.monthly ?? [];

    return (
      <div className="dash__panel">
        {/* 패널 헤더: 제목 + 현재 탭 기준 합계 */}
        <div className="dash__panel-header">
          <span className="dash__panel-title">사용량 현황</span>
          {/* totalCount가 있을 때만 합계 표시, 없으면 숨김 */}
          {data?.totalCount != null && (
            <span className="dash__panel-sub">
              합계: {data.totalCount.toLocaleString()}건
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
              // 클릭 시 부모(DashBoard.jsx)의 setUsageTab 호출
              // → UsageDetailTable도 같은 activeTab을 받으므로 함께 갱신됨
              onClick={() => onTabChange(t.id)}
            >
              {t.label}
            </button>
          ))}
        </div>

        {/* 차트 영역: 고정 높이 220px */}
        <div style={{ width: '100%', height: 220 }}>
          {loading ? (
            <div className="dash__empty">불러오는 중...</div>
          ) : monthly.length === 0 ? (
            <div className="dash__empty">데이터 없음</div>
          ) : (
            // ResponsiveContainer: 부모 너비에 맞게 차트 자동 리사이즈
            <ResponsiveContainer width="100%" height="100%">
              <LineChart
                data={monthly} // [{ month, count }] 배열
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

                {/* Y축: 눈금선/축선 숨김 */}
                <YAxis
                  tick={{ fill: '#9ca3af', fontSize: 11 }}
                  axisLine={false}
                  tickLine={false}
                />

                {/* 커스텀 툴팁 */}
                <Tooltip content={<DarkTooltip />} />

                {/* 라인: count 필드, 포인트 점 표시 */}
                <Line
                  type="monotone"      // 부드러운 곡선
                  dataKey="count"      // 사용할 데이터 필드 (백엔드 응답 monthly[].count)
                  stroke="#2563eb"     // 라인 색상
                  strokeWidth={2}
                  dot={{ fill: '#2563eb', r: 4 }}          // 데이터 포인트 점
                  activeDot={{ r: 6, fill: '#2563eb' }}    // 호버 시 점 크기
                />
              </LineChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>
    );
  }