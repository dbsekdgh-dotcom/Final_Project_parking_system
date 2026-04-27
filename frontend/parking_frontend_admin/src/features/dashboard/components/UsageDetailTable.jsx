  import { useEffect, useState } from 'react';
  import { getUsageDetail } from '../api/dashboardApi';

  const CATEGORY_LABEL = {
    PARKING:      '주차장',
    TICKET:       '할인권',
    SUBSCRIPTION: '정기권',
    RESERVATION:  '방문예약',
    POINT:        '포인트',
  };

 export default function UsageDetailTable({ activeTab }) {
    // API 응답의 content 배열 저장
    const [rows, setRows] = useState([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
      // activeTab이 바뀔 때마다 실행
      // page=0, size=5 고정 → 최신 5건만 표시
      setLoading(true);
      getUsageDetail(activeTab, 0, 5)
        .then(d => setRows(d.content ?? [])) // content 없으면 빈 배열
        .catch(() => setRows([]))            // 에러 시 빈 테이블 유지
        .finally(() => setLoading(false));
    }, [activeTab]); // activeTab이 의존성 → 탭 바뀔 때만 재호출

    return (
      <div className="dash__panel">
        {/* 패널 헤더: 제목 + 현재 선택된 탭 이름 */}
        <div className="dash__panel-header">
          <span className="dash__panel-title">사용량 상세 현황</span>
          {/* CATEGORY_LABEL에 없는 값이면 activeTab 그대로 표시 */}
          <span className="dash__panel-sub">
            {CATEGORY_LABEL[activeTab] ?? activeTab}
          </span>
        </div>

        {/* 테이블 */}
        {loading ? (
          <div className="dash__empty">불러오는 중...</div>
        ) : (
          <table className="dash__table">
            <thead>
              <tr>
                <th>날짜</th>
                <th>카테고리</th>
                <th>사용량</th>
                <th>거래건수</th>
              </tr>
            </thead>
            <tbody>
              {rows.length === 0 ? (
                // 데이터 없을 때 (백엔드 미구현 포함)
                <tr>
                  <td colSpan={4} className="dash__empty">데이터 없음</td>
                </tr>
              ) : (
                rows.map((r, i) => (
                  <tr key={i}>
                    {/* 날짜: 모노스페이스 폰트로 정렬감 있게 */}
                    <td className="dash__td--mono">{r.date}</td>

                    {/* 카테고리: 백엔드에서 내려주는 문자열 그대로 표시 */}
                    <td className="dash__td--muted">{r.category}</td>

                    {/* 사용량: 천단위 콤마 + 건 단위 */}
                    <td className="dash__td--amount">
                      {r.usageCount?.toLocaleString()}건
                    </td>

                    {/* 거래건수 */}
                    <td className="dash__td--muted">{r.transactionCount}건</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        )}
      </div>
    );
  }