import React from 'react'
import './ParkingAreaMap.css'
import ParkingSlot from './ParkingSlot'
// 주차맵 전체 컴포넌트

const ParkingAreaMap = ({spaces, floor, onSlotClick}) => {
    return (
        <div className='parking-area-map-container'>
            <div className='map-header'>
                {floor}층
            </div>
            {/* 주차공간 */}
            <div className='parking-grid'>
                {spaces.length > 0 ? (
                    spaces.map((space)=>(
                        <ParkingSlot key={space.id} space={space} onClick={()=>onSlotClick(space)}/>
                    ))
                ) : (
                    <div className='no-data'>주차 공간 데이터를 불러오는 중입니다..</div>
                )}
            </div>
        </div>
    )
}

export default ParkingAreaMap