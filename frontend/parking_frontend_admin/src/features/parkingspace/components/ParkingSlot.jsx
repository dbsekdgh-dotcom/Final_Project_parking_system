import React from 'react'
import './ParkingSlot.css'
// 주차칸 하나하나의 컴포넌트

const ParkingSlot = ({space}) => {
    const statusClass = 
    space.status === 'AVAILABLE' ? 'empty' : 
    space.status === 'OCCUPIED' ? 'occupied' : 'blocked'

    //특수구역 여부
    const specialClass = space.isDisabled ? 'disabled-zone' : space.isEvCharge ? 'ev-zone' : ''

    return (
        <div className={`parking-slot ${statusClass} ${specialClass}`}>
            {/* 상단: 주차 구역 코드 */}
            <div className='slot-header'>
                <span className='space-code'>{space.spaceCode}</span>
            </div>
            {/* 중앙: 특수 구역 아이콘 (장애인/전기차) */}
            <div className='slot-icon'>
                {space.isDisabled && <span className='icon'>♿</span>}
                {space.isEvCharge && <span className='icon'>⚡</span>}
            </div>
            {/* 하단: 차량 번호(점유중일 경우) */}
            <div className='slot-footer'>
                {space.status === 'OCCUPIED' && space.carNumber ? (
                    <span className='car-number'>{space.carNumber}</span>
                ) : (
                    <span className='empty-text'></span>
                )}
            </div>
        </div>
    )
}

export default ParkingSlot