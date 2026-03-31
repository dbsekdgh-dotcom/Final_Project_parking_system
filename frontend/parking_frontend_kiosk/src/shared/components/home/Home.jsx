import React from 'react'
import { useNavigate } from 'react-router-dom'
import './home.css'
const Home = () => {
  const navigate = useNavigate()

  return (
    <div className='full-page-container'>
      <div className="home-container">
          {/* 제목 및 구분선 */}
          <h1 className="home-title">주차 관리 시스템</h1>
          <hr className="home-divider" />

          {/* 중앙 메인 카드 섹션 */}
          <div className="card-section">
            {/* 입차 / 출차 카드 */}
            <div className="card">
              <div className="card-title">입차 / 출차</div>
              <p className="card-subtext">
                차량 번호판을 입력하고<br />
                입차 또는 출차를 선택하세요
              </p>
              <button 
                className="main-button" 
                onClick={() => navigate('/')}
              >
                입차/출차 시작
              </button>
            </div>

            {/* 사전 정산 카드 */}
            <div className="card">
              <div className="card-title">사전 정산</div>
              <p className="card-subtext">
                차량 번호판을 입력하고<br />
                주차 요금을 결제하세요
              </p>
              <button 
                className="main-button" 
                onClick={() => navigate('/prepayment')}
              >
                사전 정산 시작
              </button>
            </div>
          </div>

          {/* 하단 보조 버튼 섹션 */}
          <div className="bottom-section">
            <button className="bottom-button" onClick={() => navigate('/')}>
              상가 관리
            </button>
            <button className="bottom-button" onClick={() => navigate('/')}>
              내차 찾기
            </button>
          </div>
        </div>
    </div>
  )
}

export default Home;