import { useEffect, useState } from "react";
import { getRevenue, getUsage } from "../api/dashboardApi";

export default function DashHighlightCards() {
    // 매출 API 응답 저장
    // { totalAmount: **, changePercent: **, monthly: [..] }
    const [ revenue, setRevenue] = useState(null);
    const [ usage, setUsage ] = useState(null);

    useEffect(() =>{
        getRevenue('TOTAL').then(setRevenue).catch(()=>{});
        getUsage('TOTAL').then(setUsage).catch(()=>{});
    }, [])
     return (
      <div className="dash__highlights">
        {/*
         * 왼쪽 카드: 총 매출액
         * revenue가 null이면(API 미응답) value에 '–' 표시
         * revenue.totalAmount가 있으면 ₩ + 천단위 콤마 포맷으로 표시
         * revenue.changePercent : 전월 대비 % (양수=증가, 음수=감소)
         */}
        <HighlightCard
          label="순 매출액"
          value={
            revenue?.totalAmount != null
              ? `₩${revenue.totalAmount.toLocaleString()}`
              : '–'
          }
          change={revenue?.changePercent}
        />

        {/*
         * 오른쪽 카드: 총 사용량
         * usage가 null이면(API 미응답) value에 '–' 표시
         * usage.totalCount가 있으면 천단위 콤마 + '건' 단위로 표시
         * usage.changePercent : 전월 대비 % (양수=증가, 음수=감소)
         */}
        <HighlightCard
          label="총 사용량"
          value={
            usage?.totalCount != null
              ? `${usage.totalCount.toLocaleString()}건`
              : '–'
          }
          change={usage?.changePercent}
        />
      </div>
    );
}

// 파일 내부 전용 카드 컴포넌트
// label  : 카드 제목 (예: "총 매출액")
// value  : 이미 포맷된 표시값 (예: "₩130,318,000")
// change : 전월 대비 % (숫자), null이면 변화율 줄 숨김
function HighlightCard({ label, value, change }) {
  // change가 0 이상이면 증가(up) → 초록, 미만이면 감소(down) → 빨강
  const isUp = change == null || change >= 0;
  return (
    <div className="dash__highlight-card">
      <span className="dash__highlight-label">{label}</span>
      <span className="dash__highlight-value">{value}</span>
      {/* change가 null이면 이 줄 자체를 렌더링하지 않음 */}
      {change != null && (
        <span className={`dash__highlight-change dash__highlight-change--${isUp ? 'up' : 'down'}`}>
          {isUp ? '▲' : '▼'} {Math.abs(change)}% from last month
        </span>
      )}
    </div>
  );
}