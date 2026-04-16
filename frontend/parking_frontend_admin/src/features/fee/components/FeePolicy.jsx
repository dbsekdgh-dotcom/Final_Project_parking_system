import React, { useEffect, useState } from 'react'
import {searchFeePolicy} from './../api/feeApi'
import { useQuery } from '@tanstack/react-query'
import './feePolicy.css'
import PolicyBox from './PolicyBox';

const FeePolicy = () => {
  // const [cv,setCurrentVisitor]=useState(null)
  // const [uv,setUpcomingVisitor]=useState(null)
  // const [cr,setCurrentReservation]=useState(null)
  // const [ur,setUpcomingReservation]=useState(null)
  const [tp,setTicketPolicies]=useState([])

  const {data,isError,isLoading}=useQuery({
    queryKey:['feePolicy'],
    queryFn:async()=>await searchFeePolicy(),
  })

  useEffect(()=>{
    if(data){
      // setCurrentVisitor(data.currentVisitor || null)
      // setCurrentReservation(data.currentReservation || null)
      // setUpcomingVisitor(data.upcomingVisitor || null)
      // setUpcomingReservation(data.upcomingReservation || null)
      setTicketPolicies(data.ticketPolicies|| [])
    }

  },[data])

  return (
    <div className='container'>
        {/* 외부인 요금 정책 */}
        <PolicyBox title="외부인 요금 정책" data={data?.currentVisitor} isUpcoming={false}/>
        {data?.upcomingVisitor && <PolicyBox title="외부인 요금 정책(적용 예정)" data={data?.upcomingVisitor} isUpcoming={true}/>}

        {/* 방문객 요금 정책 */}
        <PolicyBox title="방문객 요금 정책" data={data?.currentReservation} isUpcoming={false}/>
        {data?.upcomingReservation && <PolicyBox title="방문객 요금 정책(적용 예정)" data={data?.upcomingReservation} isUpcoming={true}/>}

        {/* 할인권 정책 */}

    </div>
  )
}

export default FeePolicy;