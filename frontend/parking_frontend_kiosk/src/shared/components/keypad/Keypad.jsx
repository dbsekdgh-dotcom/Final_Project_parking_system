import React from 'react'
import './keypad.css'

const Keypad = ({onClearClick,onDeleteClick,onKeyClick}) => {
    const keys=[9,8,7,6,5,4,3,2,1,'초기화',0,'지우기']
  return (
    <div className='key-container'>
        {
            keys.map(k=>{
                //keypad에서 발생한 클릭이벤트를 부모 컴포넌트에서 zustand상태에 반영하기 위해 onkeyClick 이란 prop 생성
                return k=='초기화'?
                <button key={k} onClick={()=>onClearClick(k)} type='button' className='key-button'>{k}</button>:
                k=='지우기'?
                <button key={k} onClick={()=>onDeleteClick(k)} type='button' className='key-button'>{k}</button>:
                <button key={k} onClick={()=>onKeyClick(k)} type='button' className='key-button'>{k}</button>
            })
        }
    </div>
  )
}

export default Keypad;