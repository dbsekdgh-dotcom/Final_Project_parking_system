import React from 'react'
import './vehicleInfo.css'

const VehicleInfo = ({vehicleNumber,parkingTime,fee}) => {
  return (
    <div>
        <h2 className='page-subtitle'>차량정보</h2>
        <div className='info-card-container'>
            <div className="info-group">
                <span className="info-label">차량번호</span>
                <span className="info-value">{vehicleNumber}</span>
            </div>
            <div className="info-group">
                <span className="info-label">주차 시간</span>
                <span className="info-value">{parkingTime}</span>
            </div>
            <div className="info-group">
                <span className="info-label">주차 요금</span>
                <span className="fee-text">{fee}</span>
            </div>
        </div>
    </div>
  )
}

export default VehicleInfo;