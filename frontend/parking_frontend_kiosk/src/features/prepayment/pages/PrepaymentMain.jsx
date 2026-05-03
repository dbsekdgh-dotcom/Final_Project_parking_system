import Keypad from '../../../shared/components/keypad/Keypad';
import useVehicleStore from '../../../store/useVehicleStore';
import '../../../app.css'
import './prepayment.css'
import { useNavigate } from 'react-router-dom';

const PrepaymentMain = () => {
  const {searchKeyword,addSearchKeyword,deleteSearchKeyword,resetSearchKeyword,resetAll}=useVehicleStore()
  const navigate=useNavigate()

  const searchCarHandler=async()=>{
    if(searchKeyword=="" || searchKeyword.length<4){
      alert("차량번호 네글자를 입력해주세요")
      return
    }
    //const res=await searchCar(searchKeyword)
    navigate("/searchResult")
  }

  const resetHandler=()=>{
      resetAll()
      navigate("/")
  }


  return (
    <div className='full-page-container'>
      <div className='page-header-container'>
        <h2 className='page-title'>차량번호 입력</h2>
        <button 
          type='button' 
          className='header-back-button' onClick={resetHandler}>돌아가기</button>
      </div>
      <div className='number-display'>
        {searchKeyword}
      </div>
      {/* 자식컴포넌트에 Props 전달 */}
      <Keypad onKeyClick={value=>addSearchKeyword(value)} 
      onClearClick={resetSearchKeyword} 
      onDeleteClick={deleteSearchKeyword}></Keypad>
      <button type='button' className='ok-button' onClick={searchCarHandler}>확인</button>
    </div>

  )
}

export default PrepaymentMain;