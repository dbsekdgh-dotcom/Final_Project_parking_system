import React from 'react'
import './ParkingSpaceSummaryCards.css'

const ParkingSpaceSummaryCards = ({summaryData}) => {
    if(!summaryData){
        return (
            <div className='summary-cards-container'>
                {[1,2,3,4].map((i)=>(
                    <div key={i} className='summary-card loading'>로딩 중...</div>
                ))}
            </div>
        )
    }

    const {totalSpaces, occupiedSpaces, availableSpaces, occupancyRate} = summaryData

    return (
        <div className='summary-cards-container'>
            <div className='summary-card'>
                <span className='card-label'>전체 구획</span>
                <span className='card-value'>{totalSpaces}</span>
            </div>
            <div className='summary-card'>
                <span className='card-label'>점유</span>
                <span className='card-value'>{occupiedSpaces}</span>
            </div>
            <div className='summary-card'>
                <span className='card-label'>가용</span>
                <span className='card-value'>{availableSpaces}</span>
            </div>
            <div className='summary-card'>
                <span className='card-label'>점유율</span>
                <span className='card-value'>{occupancyRate}%</span>
            </div>
        </div>
    )
}

export default ParkingSpaceSummaryCards