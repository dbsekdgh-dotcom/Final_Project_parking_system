import React from 'react';

const formatDate = (dateStr) => {
    if (!dateStr) return '';
    const [yyyy, mm, dd] = dateStr.split('-');
    return `${yyyy}년 ${mm}월 ${dd}일`;
};

const ReservationPolicyBox = ({ policy, isLoading, date }) => {
    if (isLoading) return <div className="policy-info-box"><p>정책 확인 중...</p></div>;
    if (!policy) return null;

    const isUnlimitedDaily = policy.dailyLimitPerHousehold == null;
    const remaining = isUnlimitedDaily ? null : policy.dailyLimitPerHousehold - policy.targetDateUserCount;
    const isFull = policy.targetDateTotalCount >= policy.totalDailyLimit;

    return (
        <div className="policy-info-box">
            <div className="policy-row">
                <span>일일 잔여 횟수: </span>
                <strong>{isUnlimitedDaily ? '무제한' : `${remaining}회`}</strong>
                <small className="policy-date"> (최대 {isUnlimitedDaily ? '무제한' : `${policy.dailyLimitPerHousehold}회`})</small>
            </div>
            <div className="policy-row">
                <span>단지 전체 현황 {date && <small className="policy-date">({formatDate(date)})</small>} : </span>
                <strong className={isFull ? 'text-red' : ''}>
                    {policy.targetDateTotalCount} / {policy.totalDailyLimit}
                </strong>
            </div>
            {policy.warningMessage && (
                <p className="policy-warning">⚠️ {policy.warningMessage}</p>
            )}
        </div>
    );
};

export default ReservationPolicyBox;
