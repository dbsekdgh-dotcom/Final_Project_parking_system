import React from 'react'
import './ParkingSummaryCards.css'

const ParkingSummaryCards = ({data, onCardClick, activeFilter}) => {
    //데이터가 없을경우의 초기값 설정
    const {
        currentParkingCount = 0,
        todayExitedCount = 0,
        unpaidCount = 0,
        todayLogCount = 0
    } = data || {}

    const summaryItems =[
        {label: '현재 주차',value:currentParkingCount,unit:'대',id:'CURRENT'},
        {label: '금일 출차 완료',value:todayExitedCount,unit:'대',id:'EXITED'},
        {label: '미납',value:unpaidCount,unit:'대',id:'UNPAID'},
        {label: '금일 로그',value:todayLogCount,unit:'대',id:'LOG'},
    ]

    return (
        <div className='parking-summary'>
            {summaryItems.map((item)=>(
                <div key={item.id} className={`summary-card ${activeFilter===item.id?'active':''}`}
                onClick={()=>onCardClick(item.id)}>
                    <div className='summary-label'>{item.label}</div>
                    <div className='summary-value'>{item.value}
                        <span className='summary-unit'>{item.unit}</span>
                    </div>
                </div>
            ))}
        </div>
    )
}

export default ParkingSummaryCards