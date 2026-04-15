import React, { useCallback, useEffect, useState } from 'react'
import './ParkingSpacePage.css'
import ParkingSpaceSummaryCards from '../components/ParkingSpaceSummaryCards'
import { getParkingSpaceSummary, getParkingSpace } from '../api/parkingSpaceApi'
import ParkingAreaMap from '../components/ParkingAreaMap'

const ParkingSpacePage = () => {
  const [summary,setSummary]=useState(null)
  const [spaces,setSpaces]=useState([])
  const [currentFloor,setCurrentFloor]=useState('B1')

  const fetchData = useCallback(async()=>{
    try{
      const [summaryRes, spacesRes]=await Promise.all([
        getParkingSpaceSummary(),
        getParkingSpace(currentFloor)
      ])
      console.log(summaryRes)
      setSummary(summaryRes.data)
      setSpaces(spacesRes.data)
    }catch(error){
      console.error("실시간 데이터 갱신 실패:",error)
    }
  },[currentFloor])

  useEffect(()=>{
    //처음 진입 시 호출
    fetchData()
    //30초마다 폴링 시작
    const intervalId = setInterval(()=>{
      fetchData()
    },30000)
    //컴포넌트 언마운트 시 인터벌 해제
    return ()=>clearInterval(intervalId)
  },[fetchData])

  return (
    <div className='parking-space-page'>
      {/* 상단 요약정보 카드 영역 */}
      <ParkingSpaceSummaryCards />

      {/* 하단 층별 주차현황 리스트 영역 */}
      <div className='floor-tabs'>
        <button onClick={()=>setCurrentFloor('B1')} className={currentFloor ==='B1' ? 'active':''}>B1</button>
        <button onClick={()=>setCurrentFloor('B2')} className={currentFloor ==='B2' ? 'active':''}>B2</button>
      </div>
      {/* <ParkingAreaMap spaces={spaces}/> */}
    </div>
  )
}

export default ParkingSpacePage