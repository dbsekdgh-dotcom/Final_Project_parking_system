import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { FaMagnifyingGlass, FaCreditCard, FaPhone, FaStore, FaPlay } from 'react-icons/fa6'
import { getParkingSummary } from '../../api/ParkingApi'
import './home.css'

const Home = () => {
  const navigate = useNavigate()
  const [parkingInfo, setParkingInfo] = useState({ occupied: null, total: null })
  useEffect(() => {
    sessionStorage.removeItem('paymentFlow')
    sessionStorage.removeItem('pendingParkingLogId')
  }, [])

  useEffect(()=>{
    const fetchParkingInfo = async () => {
      try{
        const res = await getParkingSummary()
        const { occupiedSpaces, totalSpaces } = res.data.data
        setParkingInfo({ occupied: occupiedSpaces, total: totalSpaces })
      }catch{

      }
    }
    fetchParkingInfo()
    const interval = setInterval(fetchParkingInfo,30000)
    return () => clearInterval(interval)
  }, [])

  return (
    <div className="kiosk-wrapper">

        {/* 주차 현황 바 */}
        <section className="parking-info">
          <div className="info-label">PARKING AVAILABILITY</div>
          <div className="info-count">
            {parkingInfo.occupied ?? '--'} <span>/ {parkingInfo.total ?? '--'}대</span>
          </div>
        </section>

        {/* 메인 메뉴 */}
        <main className="menu-container">
          <button className="menu-box" onClick={() => navigate('/find-car')}>
            <FaMagnifyingGlass className="menu-icon" />
            <h2 className="menu-title">내 차 찾기</h2>
            <p className="menu-sub">SEARCH VEHICLE</p>
          </button>
          <button className="menu-box menu-box--filled" onClick={() => navigate('/prepayment')}>
            <FaCreditCard className="menu-icon" />
            <h2 className="menu-title">사전 정산</h2>
            <p className="menu-sub">PAYMENT FIRST</p>
          </button>
        </main>

        {/* 하단 보조 버튼 */}
        <footer className="footer-actions">
          <button className="action-btn call-btn">
            <FaPhone /> 관리자 호출
          </button>
          <button className="action-btn" onClick={() => navigate('/store/login')}>
            <FaStore /> 상가 관리
          </button>
          <button className="action-btn" onClick={() => navigate('/entry-exit')}>
            <FaPlay /> 입/출차 TEST
          </button>
        </footer>

    </div>
  )
}

export default Home
