import { useState, useEffect } from 'react';
import DashSummaryCards from '../components/DashSummaryCards'
import DashHighlightCards from '../components/DashHighlightCards'
import SalesChart from '../components/SalesChart'
import UsageChart from '../components/UsageChart'
import SalesDetailTable from '../components/SalesDetailTable'
import UsageDetailTable from '../components/UsageDetailTable'
import './DashBoard.css'

// 탭 state를 이 컴포넌트에서 중앙관리
// 차트/테이블에 props로 내려주는 구조
// salesTap : 매출 현황 탭 ( SalesChart + SalesDetailTable 공유 )
//  - 초기값 'TOTAL' -> 페이지 진입 시 전체 매출 표시
//  - 가능한 값: 'TOTAL' | 'TICKET' | 'PARKING' | 'SUBSCRIPTION'
//
// usageTab : 사용량 현황 탭 (UsageChart + UsageDetailTable 공유)
//  - 초기값 'PARKING' -> 페이지 진입 시 주차장 사용량 표시
//  - 가능한 값: 'PARKING' | 'TICKET' | 'SUBSCRIPTION' | 'RESERVATION' | 'POINT'
//
// 탭 클릭 흐름
// SalesChart 탭 클릭 -> onTabChange(id) 호출 
// -> setSalesTab(id) 실행 -> salesTab 변경
// -> SalesChart, SalesDetailTable 동시 리렌더링 + API 재호출

export default function DashBoard() {
   // 매출 현황 탭 state: SalesChart + SalesDetailTable이 함께 사용
    const [salesTab, setSalesTab] = useState('TOTAL');

    // 사용량 현황 탭 state: UsageChart + UsageDetailTable이 함께 사용
    const [usageTab, setUsageTab] = useState('PARKING');

    return (
      <div className="dash">

        {/* 1행: 상단 통계 카드 4개 (세대수/차량/주차공간/수익) */}
        <DashSummaryCards />

        {/* 2행: 총 매출액 + 총 사용량 하이라이트 카드 */}
        <DashHighlightCards />

        {/* 3행: 차트 2개 (좌-매출 바차트 / 우-사용량 라인차트) */}
        <div className="dash__charts">
          {/*
           * activeTab  : 현재 선택된 탭 id 전달
           * onTabChange: 탭 클릭 시 setSalesTab 호출 → salesTab 갱신
           */}
          <SalesChart activeTab={salesTab} onTabChange={setSalesTab} />

          {/*
           * activeTab  : 현재 선택된 탭 id 전달
           * onTabChange: 탭 클릭 시 setUsageTab 호출 → usageTab 갱신
           */}
          <UsageChart activeTab={usageTab} onTabChange={setUsageTab} />
        </div>

        {/* 4행: 상세 현황 테이블 2개 (좌-매출 / 우-사용량) */}
        <div className="dash__tables">
          {/*
           * activeTab: salesTab을 그대로 전달
           * → SalesChart 탭과 항상 동기화되어 같은 카테고리 데이터 표시
           */}
          <SalesDetailTable activeTab={salesTab} />

          {/*
           * activeTab: usageTab을 그대로 전달
           * → UsageChart 탭과 항상 동기화되어 같은 카테고리 데이터 표시
           */}
          <UsageDetailTable activeTab={usageTab} />
        </div>

      </div>
    );
}
