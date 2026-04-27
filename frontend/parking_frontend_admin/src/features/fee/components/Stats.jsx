import React from 'react'
import StatsBox from './StatsBox';
import RevenueAnalysisBox from './RevenueAnalysisBox';

const Stats = () => {
  return (
    <div className='container'>
      <StatsBox title="날짜별 현금흐름"/>
      <RevenueAnalysisBox  title="주차요금 매출 분석"/>
    </div>
  )
}

export default Stats;