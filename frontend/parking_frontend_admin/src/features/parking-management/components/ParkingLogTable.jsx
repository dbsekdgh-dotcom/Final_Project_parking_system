import React from 'react'
import './ParkingLogTable.css'

const ParkingLogTable = ({ logs, page, totalPages, onPageChange }) => {
    //ENUM값을 한글로 보기 좋게 매핑(필요시 추가/수정)
    const typeLabel = {
        RESIDENT: '입주민',
        VISIT: '외부인',
        USER: '정기권',
        RESERVATION: '방문예약'
    }

    const statusLabel={
        DETECTED: '입차시도',
        ENTRY_CANCELLED: '입차취소',
        ENTERED: '입차완료',
        EXIT_REQUESTED: '출차요청',
        EXITED: '출차완료',
        FORCE_EXITED: '강제출차'
    }

    const paymentLabel={
        NONE: '무료',
        UNPAID: '미납',
        PAID: '납부완료'
    }

    return (
        <div className='parking-table-wrapper'>
            <table className='parking-log-table'>
                <thead>
                    <tr>
                        <th>차량번호</th>
                        <th>유형</th>
                        <th>주차상태</th>
                        <th>결제상태</th>
                        <th>입차시간</th>
                        <th>출차시간</th>
                        <th>주차시간</th>
                        <th>주차위치</th>
                        <th>상세</th>
                    </tr>
                </thead>
                <tbody>
                    {logs && logs.length > 0 ? (
                        logs.map((log) => (
                            <tr key={log.parkingLogId}>
                                <td className='font-bold'>{log.carNumber}</td>
                                <td>
                                    {/* 타입에 따라 색상이 변하는 배지 처리 */}
                                    <span className={`type-badge ${log.type}`}>
                                        {typeLabel[log.type] || log.type}
                                    </span>
                                </td>
                                <td>
                                    {/* 주차상태 강조 (예: 입차완료 - 초록색) */}
                                    <span className={`status-text ${log.parkingStatus}`}>
                                        {statusLabel[log.parkingStatus] || log.parkingStatus}
                                    </span>
                                </td>
                                <td>
                                    {/* 결제상태 강조 (예: 미납 - 빨간색) */}
                                    <span className={`payment-text ${log.paymentStatus}`}>
                                        {paymentLabel[log.paymentStatus] || log.paymentStatus}
                                    </span>
                                </td>
                                <td>{log.entryTime}</td>
                                <td>{log.exitTime || '-'}</td>
                                <td>{log.parkingDuration || '-'} </td>
                                <td>{log.parkingSpaceCode || '미지정'}</td>
                                <td>
                                    <button className='detail-btn'>상세보기</button>
                                </td>
                            </tr>
                        ))
                    ) : (
                        <tr>
                            <td colSpan="9" className='no-data-cell'>
                                조회된 입출차 기록이 없습니다.
                            </td>
                        </tr>
                    )}
                </tbody>
            </table>

            {/* 페이징 UI (이전/다음 버튼) */}
            <div className='pagination-container'>
                    <button disabled={page===0} onClick={()=>onPageChange(page-1)}>이전</button>
                    <span className='page-info'>{page+1} / {totalPages}</span>
                    <button disabled={page>=totalPages-1} onClick={()=>onPageChange(page+1)}>다음</button>
            </div>
        </div>
    )
}

export default ParkingLogTable