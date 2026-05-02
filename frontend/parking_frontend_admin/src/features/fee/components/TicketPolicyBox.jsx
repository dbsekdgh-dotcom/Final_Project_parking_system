import React from 'react'
import { usePolicyMutation } from '../hooks/usePolicyMutation'
import { addTicketPolicy } from './addTicketPolicy'
import { SlPencil } from 'react-icons/sl'
import { CiTrash } from 'react-icons/ci'
import { confirmAlert } from './confirmPolicy'
import { TICKET_POLICY_STATUS_LABEL, TICKET_POLICY_TYPE_LABEL } from '../../../shared/constants/parkingLabel'

const TicketPolicyBox = ({title,data}) => {
    const {deleteTicketMutationAsync,insertTicketMutationAsync,inactivateTicketMutationAsync}=usePolicyMutation()

    const deleteHandler=(p,type)=>{
        confirmAlert({
        title:'할인권 삭제 확인',
        label:'삭제할 정책',
        value:`${p.name}(${p.discountValue}${type})`,
        effectiveDate:'즉시 적용',
        resultTitle:'삭제 완료',
        mutateAsync:deleteTicketMutationAsync,
        updatePolicy:`${p.ticketPolicyId}`
        })    
    }

    const insertTicketHandler=()=>{
        addTicketPolicy({
        updateMutateAsync:insertTicketMutationAsync
        })
    }

    const inactivateHandler=(p)=>{
        const typeLabel = TICKET_POLICY_TYPE_LABEL[p.discountType];
        const type=typeLabel==='비율'?'%':typeLabel==='시간'?'분':typeLabel==='무료'?'무료':'원'
        confirmAlert({
        title:`${p.status==='ACTIVE'?'비활성화 확인':'활성화 확인'}`,
        label:'정책 내용',
        value:`[${p.useType==='STORE'?'상가':'관리자'}] ${type==='무료'?type:p.discountValue.toLocaleString()}${type==='무료'?'':type} 할인권`,
        effectiveDate:'즉시 적용',
        resultTitle: `${p.status==='ACTIVE'?'비활성화 완료':'활성화 완료'}`,
        mutateAsync:inactivateTicketMutationAsync,
        updatePolicy:`${p.ticketPolicyId}`
        })
    }

  return (
    <div className='section'>
        <div className='sectionHeader'>
            <span className='title'>{title}</span>
            <span className='mainEdit' role='button' onClick={insertTicketHandler}><SlPencil></SlPencil></span> 
        </div>
        <div className='gridContainer'>
            {
            data && data.length>0?
            (data.map(p=>{
                const typeLabel = TICKET_POLICY_TYPE_LABEL[p.discountType];
                const type=typeLabel==='비율'?'%':typeLabel==='시간'?'분':typeLabel==='무료'?'무료':'원'
                return (
                <div  className='infoBox' key={p.ticketPolicyId}>
                    <div className='editIcon' >
                        <span className='editIcon' role='button' onClick={()=>deleteHandler(p,type)}><CiTrash/></span>
                    </div>
                    <span className='label'>[{p.useType==='STORE'?'상가':'관리자'}] {type==='무료'?type:p.discountValue.toLocaleString()}{type==='무료'?'':type} 할인권</span>
                    <span className='value'>{type==='무료'?"0": p.price.toLocaleString()}원</span>
                    <span className='label' role='button' style={{cursor: 'pointer', color:`${p.status==='ACTIVE'?'orange':'grey'}`}} onClick={()=>inactivateHandler(p)}>{TICKET_POLICY_STATUS_LABEL[p.status]}</span>
                </div>
            )})
        
            ) :(<p>등록된 할인권 정책이 없습니다.</p>)
            }
        </div>
    </div>
  )
}

export default TicketPolicyBox