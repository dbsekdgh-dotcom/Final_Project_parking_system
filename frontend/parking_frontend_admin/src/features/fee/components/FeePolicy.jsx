import React, { useEffect, useState } from 'react'
import {searchFeePolicy} from './../api/feeApi'
import { useQuery } from '@tanstack/react-query'
import './feePolicy.css'
import PolicyBox from './PolicyBox';
import { SlPencil, SlShareAlt } from 'react-icons/sl';
import {TICKET_POLICY_TYPE_LABEL,TICKET_POLICY_STATUS_LABEL} from './../../../shared/constants/parkingLabel'
import { CiTrash } from "react-icons/ci";
import { confirmAlert} from './confirmPolicy';
import {addTicketPolicy} from './addTicketPolicy'
import TicketPolicyBox from './TicketPolicyBox';

const FeePolicy = () => {
  const [isOpen,setIsOpen]=useState(false);
  const [isReservationOpen, setReservationOpen]=useState(false)

  const {data,isError,isLoading}=useQuery({
    queryKey:['feePolicy'],
    queryFn:async()=>await searchFeePolicy(),
  })

  return (
    <div className='container'>
        {/* 외부인 요금 정책 */}
        <PolicyBox title="외부인 요금 정책" data={data?.currentVisitor} isUpcoming={false} isLatest={!data?.upcomingVisitor}/>
        {
          data?.upcomingVisitor &&
          <div className="upcomingSection">
            <div onClick={()=>setIsOpen(!isOpen)} className="upcomingToggle" >
              <span>적용 예정 정책 {isOpen?'닫기 ▲':'보기 ▼' }</span>
            </div>
            {isOpen &&
              <div className="upcomingContent">
                <PolicyBox title="외부인 요금 정책(적용 예정)" data={data?.upcomingVisitor} isUpcoming={true} isLatest={true}/>
              </div>
            }
          </div>
        }
        
        {/* 방문객 요금 정책 */}
        <PolicyBox title="방문객 요금 정책" data={data?.currentReservation} isUpcoming={false} isLatest={!data?.upcomingReservation}/>
        {
          data?.upcomingReservation &&
          <div className="upcomingSection">
            <div onClick={()=>setReservationOpen(!isReservationOpen)} className="upcomingToggle" >
              <span>적용 예정 정책 {isOpen?'닫기 ▲':'보기 ▼' }</span>
            </div>
            {
              isReservationOpen &&
              <div className="upcomingContent">
                <PolicyBox title="방문객 요금 정책(적용 예정)" data={data?.upcomingReservation} isUpcoming={true} isLatest={true}/>
              </div>
            }
          </div>
        }

        {/* 할인권 정책 */}
        <TicketPolicyBox title="[상가] 할인권 정책" data={data?.storeTicket}/>
        <TicketPolicyBox title="[관리자] 할인권 정책" data={data?.adminTicket}/>
        <TicketPolicyBox title="[관리자] 상가 기본지급 할인권 정책" data={data?.monthlyTicket}/>
    </div>
  )
}

export default FeePolicy;