import React, { useCallback, useEffect, useState } from 'react'
import './ParkingSpacePage.css'
import ParkingSpaceSummaryCards from '../components/ParkingSpaceSummaryCards'
import { getParkingSpaceSummary, getParkingSpace } from '../api/parkingSpaceApi'
import ParkingAreaMap from '../components/ParkingAreaMap'
import ParkingFloorStatus from '../components/ParkingFloorStatus'
import { __controlParkingSpace, clearStatus } from '../slices/ParkingSpaceSlice'
import { useDispatch, useSelector } from 'react-redux'
import ParkingControlModal from '../components/ParkingControlModal'

const ParkingSpacePage = () => {
  const [summary, setSummary] = useState(null)
  const [spaces, setSpaces] = useState([])
  const [currentFloor, setCurrentFloor] = useState('B1')
  const [loading, setLoading] = useState(false)
  const [selectedSpace, setSelectedSpace] = useState(null) //모달에 표시할 데이터
  const [isModalOpen, setIsModalOpen] = useState(false)

  const dispatch = useDispatch()
  const { successMessage, error } = useSelector((state) => state.parkingSpace)

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

  //주차 칸 클릭시 호출될 함수
  const handleSlotClick = (space) => {
    if (space.status === 'OCCUPIED') {
      alert(`현재 ${space.carNumber || '차량'}이(가) 주차 중입니다. 주차 중인 구역은 제어할 수 없습니다.`)
      return
    }
    setSelectedSpace(space)
    setIsModalOpen(true)
  }

  const handleControl = async (id, action) => {
    try {
      await dispatch(__controlParkingSpace({ spaceId: id, action: action })).unwrap()
      //변경 성공시 즉시 리스트 갱신
      await fetchFloorSpaces()
      fetchSummary()
      setIsModalOpen(false)
    } catch (error) {
      console.error("Control error:",error)
      // alert(typeof error === 'string' ? error : "처리에 실패했습니다.")
    }
  }

  //Redux 성공/실패 메시지 감시
  useEffect(() => {
    if (successMessage) {
      alert(successMessage)
      setIsModalOpen(false)
      dispatch(clearStatus())
      fetchSummary() // 요약 정보 갱신
      fetchFloorSpaces() // 층별 정보 갱신
    }
    if (error) {
      alert(error)
      dispatch(clearStatus())
    }
  }, [successMessage, error, dispatch, fetchFloorSpaces])

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
            <ParkingAreaMap spaces={spaces} floor={currentFloor} onSlotClick={handleSlotClick} />
          </div>
        </div>

        {/* 하단 우측 구역별 현황 컴포넌트 */}
        <div className='right-area'>
          <ParkingFloorStatus summaryData={summary} currentFloor={currentFloor} />
        </div>
      </div>

      {/* 제어 모달 */}
      <ParkingControlModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)}
        selectedSpace={selectedSpace} onBlock={(id) => handleControl(id, 'BLOCK')}
        onUnblock={(id) => handleControl(id, 'UNBLOCK')}
        onChangeType={handleControl} />
    </div>
  )
}

export default ParkingSpacePage