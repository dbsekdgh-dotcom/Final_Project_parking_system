import { useQuery } from '@tanstack/react-query';
import React from 'react'
import {searchPolicyHistory} from './../api/feeApi'
import HistoryBox from './HistoryBox';

const FeeHistory = () => {
  const {data,isLoading,isError}=useQuery({
    queryKey:['policyHistory'],
    queryFn:async()=>await searchPolicyHistory()
  })


  return (
    <div className='container'>
      {data && <HistoryBox title='외부인 요금 정책' data={data?.visitorPolicyHistory}/>}
      {data && <HistoryBox title='방문객 요금 정책' data={data?.reservationPolicyHistory}/>}
    </div>
  )
}

export default FeeHistory;