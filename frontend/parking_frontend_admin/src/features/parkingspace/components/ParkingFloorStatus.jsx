import React from 'react'
import './ParkingFloorStatus.css'
import { PARKING_SPACE_STATUS_LABEL } from '../../../shared/constants/parkingLabel'

const ParkingFloorStatus = ({ summaryData, currentFloor }) => {
    if (!summaryData) {
        return (
            <div className='parking-floor-status loading-state'>
                <div className='status-skeleton title'></div>
                <div className='status-skeleton bar'></div>
                <div className='status-skeleton bar'></div>
            </div>
        )
    }

    const { b1Total, b2Total, b1Occupied, b2Occupied } = summaryData

    //현재 층에 따른 데이터 동적 할당
    const isB1 = currentFloor === 'B1'
    const total = isB1 ? b1Total : b2Total
    const occupied = isB1 ? b1Occupied : b2Occupied
    const avaliable = total - occupied
    const blocked = 0; //차단 데이터가 API에 추가되면 연결예정

    //점유율 계산
    const getRate = (occ, tot) => (tot > 0 ? Math.round((occ / tot) * 100) : 0)

    return (
        <div className='parking-floor-status'>
            {/* 범례 */}
            <div className='status-section'>
                <h4 className='section-title'>범례</h4>
                <ul className='legend-list'>
                    <li>
                        <div className='legend-item-left'>
                            <span className='dot empty'></span>
                            {PARKING_SPACE_STATUS_LABEL.AVAILABLE}
                        </div>
                        <span className='legend-count'>{avaliable}</span>
                    </li>
                    <li>
                        <div className='legend-item-left'>
                            <span className='dot occupied'></span>
                            {PARKING_SPACE_STATUS_LABEL.OCCUPIED}
                        </div>
                        <span className='legend-count'>{occupied}</span>
                    </li>
                    <li>
                        <div className='legend-item-left'>
                            <span className='dot blocked'></span>
                            {PARKING_SPACE_STATUS_LABEL.BLOCKED}
                        </div>
                        <span className='legend-count'>{blocked}</span>
                    </li>
                </ul>
            </div>

            {/* 구역별 현황 */}
            <div className='status-section'>
                <h4 className='section-title'>층별 현황</h4>
                <div className='floor-progress-list'>
                    {/* B1게이지 */}
                    <div className='floor-progress-item'>
                        <div className='floor-info'>
                            <span>B1</span>
                            <span>{b1Occupied} / {b1Total}</span>
                        </div>
                        <div className='progress-bar-bg'>
                            <div className='progress-bar-fill' style={{ width: `${getRate(b1Occupied, b1Total)}%` }}></div>
                        </div>
                        <span className='progress-text'>{getRate(b1Occupied, b1Total)}% {PARKING_SPACE_STATUS_LABEL.OCCUPIED}</span>
                    </div>
                    {/* B2게이지 */}
                    <div className='floor-progress-item'>
                        <div className='floor-info'>
                            <span>B2</span>
                            <span>{b2Occupied} / {b2Total}</span>
                        </div>
                        <div className='progress-bar-bg'>
                            <div className='progress-bar-fill' style={{ width: `${getRate(b2Occupied, b2Total)}%` }}></div>
                        </div>
                        <span className='progress-text'>{getRate(b2Occupied, b2Total)}% {PARKING_SPACE_STATUS_LABEL.OCCUPIED}</span>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default ParkingFloorStatus