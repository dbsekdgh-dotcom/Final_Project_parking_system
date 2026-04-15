import React from 'react'
import { useReservationPolicy } from '../hooks/useReservation';
import './ReservationSummary.css';

// 1. targetDate를 props로 받거나 기본값 처리
const ReservationSummary = ({ targetDate }) => {

    const defaultDate = new Date();
    // 당일 예약 불가 정책이므로 내일 날짜를 기본값으로 설정
    defaultDate.setDate(defaultDate.getDate() + 1);
    
    // Props로 넘어온 targetDate가 없으면 내일 날짜 사용
    const formattedDate = targetDate || defaultDate.toISOString().split('T')[0];

    // 2. useQuery의 결과는 'data'라는 키로 들어옵니다 (date -> data)
    const { data: policy, isLoading } = useReservationPolicy(formattedDate);

    if (isLoading) return <div className='summary-loading'>정책 로딩 중...</div>;
    if (!policy) return null;

    return (
        <div className="reservation-summary-bar">
            <div className="summary-item">
                <span className="summary-label">이번 달 누적 이용</span>
                <span className="summary-value">
                    <strong>{policy.monthUsedCount}</strong> / {policy.monthlyLimit}회
                </span>
            </div>
            <div className="summary-divider" />
            <div className="summary-item">
                <span className="summary-label">1일 최대 신청</span>
                <span className="summary-value">
                    <strong>{policy.dailyLimitPerHousehold}</strong>회
                </span>
            </div>
            <div className="summary-divider" />
            <div className="summary-item">
                <span className="summary-label">동시 예약 가능</span>
                <span className="summary-value">
                    <strong>{policy.maxActiveReservations}</strong>건
                </span>
            </div>
        </div>
    );
};

export default ReservationSummary;