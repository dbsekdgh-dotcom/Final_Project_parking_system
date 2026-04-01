import React, { useState } from 'react'
import './paymentMethod.css'

const PaymentMethod = ({fee,userPoint}) => {
    const [usePoint,setUsePoint]=useState(0)
    const [payMethod, setPayMethod] = useState('');

    const pointUseHandler=(e)=>{
        const use=e.target.value==''?0:Math.min(e.target.value,userPoint,fee)
        setUsePoint(use)
    }
    const fullPointUseHandler=()=>{
        const use=Math.min(userPoint,fee)
        setUsePoint(use)
    }
    const payMethodHandler=(e)=>{
        setPayMethod(e)
    }
    const payBtnHandler=()=>{
        if(payMethod==''){
            alert("결제 방법을 선택하세요")
        }
    }

  return (
    <div className='payment-method-container'>
        <div>
            <p>결제 방법</p>
            {/* 포인트 선택영역 */}
            {userPoint>0 &&
            <div className='payment-box point-box'>
                <div className='point-header'>
                    <span>포인트 사용 (보유 : {userPoint.toLocaleString()})</span>
                </div>
                <div className='point-input-group'>
                    <input type='number' placeholder='0' className='point-input' onChange={(e)=>pointUseHandler(e)} value={usePoint==0?'':usePoint}></input>
                    <button type='button' className='full-use-btn' onClick={fullPointUseHandler}>전액 사용</button>
                </div>
                
            </div>
            }
            {/* 신용카드 선택 영역 */}
            <div className={`payment-box card-box ${payMethod=='CARD'?'active':''}`} onClick={()=>payMethodHandler('CARD')}>
                <span>신용카드</span>
            </div>
        </div>
        <button type='button' className='final-pay-btn' onClick={payBtnHandler}>결제하기</button>
    </div>
  )
}

export default PaymentMethod;