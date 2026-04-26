import { useEffect, useState } from 'react';
import { getRevenueDetail } from "../api/dashboardApi";

// 탭 id -> 화면 표시 한글 레이블 매핑
// SelesChart의 TABS id와 반드시 일치
const CATEGORY_LABEL = {
    TOTAL:        '전체',
    TICKET:       '할인권',
    PARKING:      '주차',
    SUBSCRIPTION: '정기권',
  };



// Props:
//  activeTab {string} - DashBoard.jsx의 salesTab state SalesChart 와 같은 값을 공유함
// 백엔드 응답 형태 ( 참고 )
// {
//   content : [
//     {
//         date: "2024-04-26",  // 날짜 (String)
//         category: "할인권",   // 카테고리 이름
//         amount: 45000,       // 해당 날짜 매출액(원)
//         transactionCount: 15 // 거래 건수
//     },
//     ...
//   ],
//   totalPages: 5, //전체 페이지 수
//   totalElements: 25 // 전체 데이터 수
// }
export default function SalesDetailTable({ activeTab }) {
    // API 응답의 content 배열 저장
    const [rows, setRows] = useState([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
      // activeTab이 바뀔 때마다 실행
      // page=0, size=5 고정 → 최신 5건만 표시
      setLoading(true);
      getRevenueDetail(activeTab, 0, 5)
        .then(d => setRows(d.content ?? [])) // content 없으면 빈 배열
        .catch(() => setRows([]))            // 에러 시 빈 테이블 유지
        .finally(() => setLoading(false));
    }, [activeTab]); // activeTab이 의존성 → 탭 바뀔 때만 재호출

    return (
      <div className="dash__panel">
        {/* 패널 헤더: 제목 + 현재 선택된 탭 이름 */}
        <div className="dash__panel-header">
          <span className="dash__panel-title">순매출 상세 현황</span>
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
                <th>금액</th>
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

                    {/* 금액: ₩ + 천단위 콤마 */}
                    <td className="dash__td--amount">
                      ₩{r.amount?.toLocaleString()}
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