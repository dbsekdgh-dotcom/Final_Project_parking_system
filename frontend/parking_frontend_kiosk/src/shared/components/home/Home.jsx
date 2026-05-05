import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { FaMagnifyingGlass, FaCreditCard, FaPhone, FaStore, FaPlay } from 'react-icons/fa6'
import { getParkingSummary } from '../../api/ParkingApi'
import './home.css'

const Home = () => {
  const navigate = useNavigate()
  const [time, setTime] = useState('')
  const [date, setDate] = useState('')
  const [parkingInfo, setParkingInfo] = useState({ available:null,total:null})
  useEffect(() => {
    localStorage.removeItem('paymentFlow')
    localStorage.removeItem('pendingParkingLogId')

    const updateClock = () => {
      const now = new Date()
      const h = String(now.getHours()).padStart(2, '0')
      const m = String(now.getMinutes()).padStart(2, '0')
      setTime(`${h}:${m}`)
      setDate(now.toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' }))
    }
    updateClock()
    const timer = setInterval(updateClock, 1000)
    return () => clearInterval(timer)
  }, [])

  useEffect(()=>{
    const fetchParkingInfo = async () => {
      try{
        const res = await getParkingSummary()
        const { availableSpaces, totalSapces } = res.data.data
        setParkingInfo({ available: availableSpaces, total: totalSapces})
      }catch{

      }
    }
    fetchParkingInfo()
    const interval = setInterval(fetchParkingInfo,30000)
    return () => clearInterval(interval)
  }, [])

  return (
    <div className="home-root">
      <div className="kiosk-wrapper">

        {/* 헤더 */}
        <header className="kiosk-header">
          <div className="brand-title">PARKING CENTER</div>
          <div className="time-display">
            <div className="clock">{time}</div>
            <div className="date">{date}</div>
          </div>
        </header>

        {/* 주차 현황 바 */}
        <section className="parking-info">
          <div className="info-label">PARKING AVAILABILITY</div>
          <div className="info-count">
            {parkingInfo.available ?? '--'} <span>/ {parkingInfo.total ?? '--'}대</span>
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
            <FaStore /> 관리
          </button>
          <button className="action-btn" onClick={() => navigate('/entry-exit')}>
            <FaPlay /> 입/출차 TEST
          </button>
        </footer>

      </div>
    </div>
  )
}

export default Home
