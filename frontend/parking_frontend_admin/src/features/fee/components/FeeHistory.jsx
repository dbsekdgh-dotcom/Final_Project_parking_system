import { useQuery } from '@tanstack/react-query';
import React from 'react'
import {searchPolicyHistory} from './../api/feeApi'
import HistoryBox from './HistoryBox';

const FeeHistory = () => {
  const {data,isLoading,isError}=useQuery({
    queryKey:['policyHistory'],
    queryFn:async()=>await searchPolicyHistory()
  })
  
  if (isLoading) return
    <div className="loading-spinner">이력을 불러오는 중입니다.</div>;
  if (isError) return
    <div className="error-msg">이력을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.</div>;

  return (
    <div className='container'>
      {data && <HistoryBox title='외부인 요금 정책' data={data?.visitorPolicyHistory}/>}
      {data && <HistoryBox title='방문객 요금 정책' data={data?.reservationPolicyHistory}/>}
    </div>
  )
}

export default FeeHistory;