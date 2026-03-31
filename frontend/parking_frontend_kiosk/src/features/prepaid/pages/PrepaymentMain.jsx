import React from 'react'
import NumberInput from '../../../shared/components/keypad/Keypad';
import Keypad from '../../../shared/components/keypad/Keypad';
import useKeypadStore from '../../../store/keypadStore';
import '../../../app.css'
import './prepaymentMain.css'

const PrepaymentMain = () => {
  const {carNumber,addCarNumber,deleteCarNumber,resetCarNumber}=useKeypadStore()

  return (
    <div className='full-page-container'>
      <h2 className='page-title'>차량 뒷번호 입력</h2>
      <div className='number-display'>
        {carNumber}
      </div>
      {/* 자식컴포넌트에 Props 전달 */}
      <Keypad onKeyClick={value=>addCarNumber(value)} 
      onClearClick={resetCarNumber} 
      onDeleteClick={deleteCarNumber}></Keypad>
    </div>
  )
}

export default PrepaymentMain;