import React, { useCallback, useEffect, useState } from 'react'
import './ParkingSpacePage.css'
import ParkingSpaceSummaryCards from '../components/ParkingSpaceSummaryCards'
import { getParkingSpaceSummary, getParkingSpace } from '../api/parkingSpaceApi'
import ParkingAreaMap from '../components/ParkingAreaMap'
import ParkingFloorStatus from '../components/ParkingFloorStatus'

const ParkingSpacePage = () => {
  const [summary, setSummary] = useState(null)
  const [spaces, setSpaces] = useState([])
  const [currentFloor, setCurrentFloor] = useState('B1')
  const [loading, setLoading] = useState(false)

  const fetchSummary = async () => {
    try {
      const resp = await getParkingSpaceSummary()
      setSummary(resp.data)
    } catch (err) {
      console.error("요약 정보 갱신 실패:", err)
    }
  }

  const fetchFloorSpaces = useCallback(async () => {
    setLoading(true)
    try {
      const resp = await getParkingSpace(currentFloor)
      setSpaces(resp.data)
    } catch (err) {
      console.error("층별 주차 현황 갱신 실패:", err)
    } finally {
      setLoading(false)
    }
  }, [currentFloor])

  useEffect(() => {
    //처음 진입 시 호출
    fetchSummary()
    fetchFloorSpaces()
    //60초마다 폴링 시작
    const intervalId = setInterval(() => {
      fetchSummary()
      fetchFloorSpaces()
    }, 60000)
    //컴포넌트 언마운트 시 인터벌 해제
    return () => clearInterval(intervalId)
  }, [fetchFloorSpaces])

  return (
    <div className='parking-space-page'>
      {loading && <div className='loading-spinner'>데이터 갱신중...</div>}
      {/* 상단 요약정보 카드 영역 */}
      <ParkingSpaceSummaryCards summaryData={summary} />
      
      <div className='main-content-layout'>
        {/* 하단 좌측 층별 주차현황 리스트 영역 */}
        <div className='left-area'>
          <div className='floor-tabs'>
            <button onClick={() => setCurrentFloor('B1')} className={currentFloor === 'B1' ? 'active' : ''}>B1</button>
            <button onClick={() => setCurrentFloor('B2')} className={currentFloor === 'B2' ? 'active' : ''}>B2</button>
          </div>
          <div className={loading ? 'map-loading' : ''}>
            <ParkingAreaMap spaces={spaces} floor={currentFloor} />
          </div>
        </div>

        {/* 하단 우측 구역별 현황 컴포넌트 */}
        <div className='right-area'>
          <ParkingFloorStatus summaryData={summary} currentFloor={currentFloor}/>
        </div>
      </div>
    </div>
  )
}

export default ParkingSpacePage