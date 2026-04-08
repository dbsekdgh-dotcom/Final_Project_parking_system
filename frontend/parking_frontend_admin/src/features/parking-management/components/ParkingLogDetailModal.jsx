import React from 'react'
import './ParkingLogDetailModal.css'

const ParkingLogDetailModal = ({isOpen, data, onClose}) => {
    // 모달이 닫혀있거나 데이터가 없으면 아무것도 렌더링하지 않음
    if(!isOpen || !data) return null;

    return (
        <div>
            
        </div>
    )
}

export default ParkingLogDetailModal