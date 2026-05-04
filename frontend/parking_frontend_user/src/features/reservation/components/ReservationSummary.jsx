import React from 'react'
import { useReservationPolicy } from '../hooks/useReservation';
import './ReservationSummary.css';

// 1. targetDate를 props로 받거나 기본값 처리
const ReservationSummary = ({ targetDate }) => {

    const today = new Date().toISOString().split('T')[0];
    const formattedDate = targetDate || today;

    const { data: policy, isLoading } = useReservationPolicy(formattedDate);

    if (isLoading) return <div className='summary-loading'>정책 로딩 중...</div>;
    if (!policy) return null;

    const monthlyRemaining = policy.monthlyLimit == null
        ? '무제한'
        : `${Math.max(0, policy.monthlyLimit - policy.monthUsedCount)}회`;

    const dailyRemaining = policy.dailyLimitPerHousehold == null
        ? '무제한'
        : `${Math.max(0, policy.dailyLimitPerHousehold - policy.targetDateUserCount)}회`;

    const activeRemaining = Math.max(0, policy.maxActiveReservations - policy.currentActiveCount);

    return (
        <div className="reservation-summary-bar">
            <div className="summary-item">
                <span className="summary-label">이번 달 잔여 횟수</span>
                <strong className={`summary-main ${policy.monthlyLimit != null && policy.monthUsedCount >= policy.monthlyLimit ? 'summary-value--warn' : ''}`}>
                    {monthlyRemaining}
                </strong>
                <span className="summary-policy">
                    {policy.monthlyLimit == null ? '한도 없음' : `월 한도 ${policy.monthlyLimit}회`}
                </span>
            </div>
            <div className="summary-divider" />
            <div className="summary-item">
                <span className="summary-label">오늘 신청 잔여</span>
                <strong className={`summary-main ${policy.dailyLimitPerHousehold != null && policy.targetDateUserCount >= policy.dailyLimitPerHousehold ? 'summary-value--warn' : ''}`}>
                    {dailyRemaining}
                </strong>
                <span className="summary-policy">
                    {policy.dailyLimitPerHousehold == null ? '한도 없음' : `일 한도 ${policy.dailyLimitPerHousehold}회`}
                </span>
            </div>
            <div className="summary-divider" />
            <div className="summary-item">
                <span className="summary-label">동시 예약 가능</span>
                <strong className={`summary-main ${activeRemaining === 0 ? 'summary-value--warn' : ''}`}>
                    {activeRemaining === 0 ? '불가' : `${activeRemaining}건`}
                </strong>
                <span className="summary-policy">최대 {policy.maxActiveReservations}건</span>
            </div>
        </div>
    );
};

export default ReservationSummary;