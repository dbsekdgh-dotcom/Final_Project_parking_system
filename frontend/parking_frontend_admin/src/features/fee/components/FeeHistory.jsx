import React from 'react'
import HistoryBox from './HistoryBox';

const FeeHistory = () => {
  return (
    <div className='fee-page-container'>
      <HistoryBox title='외부인 요금 정책' parkingType='VISIT'/>
      <HistoryBox title='방문객 요금 정책' parkingType='RESERVATION'/>
    </div>
  );
};

export default FeeHistory;
