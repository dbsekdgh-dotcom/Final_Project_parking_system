import React, { useState } from 'react'
import './paymentMethod.css'

const PaymentMethod = ({fee,userPoint,onConfirm,isLoading}) => {
    const [usePoint,setUsePoint]=useState(0)

    const pointUseHandler=(e)=>{
        const use=e.target.value==''?0:Math.min(e.target.value,userPoint,fee)
        setUsePoint(use)
    }
    const fullPointUseHandler=()=>{
        const use=Math.min(userPoint,fee)
        setUsePoint(use)
    }

    const payBtnHandler=()=>{
        const paymentData={
            "usedPoint":usePoint,
            "paidAmount":fee-usePoint
        }
        console.log("usedPoint===>",usePoint,"paidAmount===>",fee-usePoint)
        onConfirm(paymentData)
    }

  return (
    <div className='payment-method-container'>
        <div>
            {/* 포인트 선택영역 */}
            {userPoint>0 &&
            <div className='payment-box point-box'>
                <div className='point-header'>
                    <span>포인트 사용 (보유 : {userPoint?.toLocaleString()})</span>
                </div>
                <div className='point-input-group'>
                    <input type='number' placeholder='0' className='point-input' onChange={(e)=>pointUseHandler(e)} value={usePoint==0?'':usePoint}></input>
                    <button type='button' className='full-use-btn' onClick={fullPointUseHandler}>전액 사용</button>
                </div>
                
            </div>
            }
        </div>
        <button type='button' className='final-pay-btn' onClick={payBtnHandler} disabled={isLoading}>
            {fee-usePoint==0? '정산 완료하기': '결제하기'}
        </button>
    </div>
  )
}

export default PaymentMethod;