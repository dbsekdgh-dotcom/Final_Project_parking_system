import React from 'react'
import './vehicleInfo.css'

const VehicleInfo = ({vehicleNumber,parkingTime,fee}) => {
    let d=0;
    let h=0;
    let m=0;
   
    d=Math.floor(parkingTime/1440);
    h=Math.floor((parkingTime-(d*1440))/60);
    m=parkingTime%60;

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
                <span className="info-value">
                    {d>0? `${d}일 `:''}
                    {h>0? `${h}시간 `:''}
                    {m>0? `${m}분 `:''}
                </span>
            </div>
            <div className="info-group">
                <span className="info-label">주차 요금</span>
                <span className="fee-text">{fee==0?'무료':(fee?.toLocaleString()??'0')+'원'}</span>
            </div>
        </div>
    </div>
  )
}

export default VehicleInfo;