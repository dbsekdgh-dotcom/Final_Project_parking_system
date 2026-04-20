import React, { useEffect, useState } from 'react'
import {searchFeePolicy} from './../api/feeApi'
import { useQuery } from '@tanstack/react-query'
import './feePolicy.css'
import PolicyBox from './PolicyBox';
import { SlPencil, SlShareAlt } from 'react-icons/sl';
import {TICKET_POLICY_TYPE_LABEL,TICKET_POLICY_STATUS_LABEL} from './../../../shared/constants/parkingLabel'
import { CiTrash } from "react-icons/ci";
import {usePolicyMutation} from './../hooks/usePolicyMutation'
import { confirmAlert} from './confirmPolicy';
import {addTicketPolicy} from './addTicketPolicy'

const FeePolicy = () => {
  const [isOpen,setIsOpen]=useState(false);
  const [isReservationOpen, setReservationOpen]=useState(false)
  const {deleteTicketMutationAsync,insertTicketMutationAsync}=usePolicyMutation()

  const {data,isError,isLoading}=useQuery({
    queryKey:['feePolicy'],
    queryFn:async()=>await searchFeePolicy(),
  })

  const deleteHandler=(p,type)=>{
    confirmAlert({
      title:'할인권 정책 삭제 확인',
      label:'삭제할 정책',
      value:`${p.name}(${p.discountValue}${type})`,
      effectiveDate:'즉시 적용',
      resultTitle:'할인권 정책 삭제 완료',
      mutateAsync:deleteTicketMutationAsync,
      updatePolicy:`${p.ticketPolicyId}`
    })
    console.log("deleteTicketMutationAsync:", deleteTicketMutationAsync)                                                                                                                                                                                         
    console.log("ticketPolicyId:", p.ticketPolicyId)      
  }

  const insertTicketHandler=()=>{
    addTicketPolicy({
      updateMutateAsync:insertTicketMutationAsync
    })
  }

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
        <div className='section'>
          <div className='sectionHeader'>
            <span className='title'>할인권 정책</span>
            <span className='mainEdit' role='button' onClick={insertTicketHandler}><SlPencil></SlPencil></span> 
          </div>
        <div className='gridContainer'>
          {
            data?.ticketPolicies && data.ticketPolicies.length>0?
            (data.ticketPolicies.map(p=>{
              const typeLabel = TICKET_POLICY_TYPE_LABEL[p.discountType];
              const type=typeLabel==='비율'?'%':typeLabel==='시간'?'분':typeLabel==='무료'?'무료':'원'
              return (
                <div  className='infoBox' key={p.ticketPolicyId}>
                <div className='editIcon' >
                  <span className='editIcon' role='button' onClick={()=>deleteHandler(p,type)}><CiTrash/></span>
                </div>
                <span className='label'>{p.name}</span>
                <span className='value'>{type==='무료'?type:p.discountValue.toLocaleString()}{type==='무료'?'':type}</span>
                <span className='label' role='button' style={{cursor: 'pointer', color:`${p.status==='ACTIVE'?'orange':'grey'}`}}>{TICKET_POLICY_STATUS_LABEL[p.status]}</span>
              </div>
            )})
      
            ) :(<p>등록된 할인권 정책이 없습니다.</p>)
          }
        </div>
        </div>

    </div>
  )
}

export default FeePolicy;