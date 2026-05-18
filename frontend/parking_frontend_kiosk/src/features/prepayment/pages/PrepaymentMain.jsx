import Keypad from '../../../shared/components/keypad/Keypad';
import useVehicleStore from '../../../store/useVehicleStore'
import '../../../app.css'
import './prepayment.css'
import { useNavigate } from 'react-router-dom';

const PrepaymentMain = () => {
  const { searchKeyword, addSearchKeyword, deleteSearchKeyword, resetSearchKeyword, resetAll } = useVehicleStore()
  const navigate = useNavigate()

  const searchCarHandler = async () => {
    if (searchKeyword == "" || searchKeyword.length < 4) {
      alert("차량번호 네글자를 입력해주세요")
      return
    }
    navigate("/searchResult")
  }

  const resetHandler = () => {
    resetAll()
    navigate("/")
  }

  return (
    <div className="kiosk-page-wrapper">
        <div className="kiosk-page-header">
          <h2 className="page-title">사전 정산</h2>
          <button type="button" className="header-back-button" onClick={resetHandler}>돌아가기</button>
        </div>
        <p className="findcar-search-label" style={{ marginBottom: '8px', letterSpacing: '0.12em', fontSize: '13px', fontWeight: 700, color: 'var(--muted)', textTransform: 'uppercase' }}>
          차량번호 끝 4자리
        </p>
        <div className="number-display">
          {searchKeyword || '- - - -'}
        </div>
        <Keypad
          onKeyClick={value => addSearchKeyword(value)}
          onClearClick={resetSearchKeyword}
          onDeleteClick={deleteSearchKeyword}
        />
        <button type="button" className="ok-button" onClick={searchCarHandler}>확인</button>
    </div>
  )
}

export default PrepaymentMain;
