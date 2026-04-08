import React from 'react'
import './ParkingLogTable.css'
import Pagination from '../../../shared/components/pagination/Pagination'
import { PARKING_STATUS_LABELS, PARKING_TYPE_LABELS, PAYMENT_STATUS_LABELS } from '../../../shared/constants/parkingLabel'

const ParkingLogTable = ({ logs, page, totalPages, onPageChange, onShowDetail }) => {

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
                                        {PARKING_TYPE_LABELS[log.type] || log.type}
                                    </span>
                                </td>
                                <td>
                                    {/* 주차상태 강조 (예: 입차완료 - 초록색) */}
                                    <span className={`status-text ${log.parkingStatus}`}>
                                        {PARKING_STATUS_LABELS[log.parkingStatus] || log.parkingStatus}
                                    </span>
                                </td>
                                <td>
                                    {/* 결제상태 강조 (예: 미납 - 빨간색) */}
                                    <span className={`payment-text ${log.paymentStatus}`}>
                                        {PAYMENT_STATUS_LABELS[log.paymentStatus] || log.paymentStatus}
                                    </span>
                                </td>
                                <td>
                                    {log.entryTime}
                                    {log.parkingStatus==='ENTRY_CANCELLED' && (
                                        <div><span className='cancel-note'>(취소)</span></div>
                                    )}
                                </td>
                                <td>{log.parkingStatus ==='EXITED' ? log.exitTime : '-'}</td>
                                <td>{log.parkingStatus==='ENTRY_CANCELLED' ? '-' : (log.parkingDuration || '-')} </td>
                                <td>{log.parkingSpaceCode || '미지정'}</td>
                                <td>
                                    <button className='detail-btn' onClick={()=>onShowDetail(log.parkingLogId)}>상세보기</button>
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

            {/* 페이징 UI */}
            <Pagination page={page} totalPages={totalPages} onPageChange={onPageChange}/>
            
        </div>
    )
}

export default ParkingLogTable