import React from 'react';
import { useSubscriptionHistory } from '../hooks/useSubscription';
import './SubscriptionHistoryList.css';

const STATUS_LABEL = {
    ACTIVE: '이용 중',
    EXPIRED: '만료',
    REFUNDED: '환불',
    CANCELLED: '취소',
};

const STATUS_CLASS = {
    ACTIVE: 'sub-history-badge--active',
    EXPIRED: 'sub-history-badge--expired',
    REFUNDED: 'sub-history-badge--refunded',
    CANCELLED: 'sub-history-badge--cancelled',
};

const formatDate = (dateStr) => {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' });
};

const SubscriptionHistoryList = () => {
    const { data: history, isLoading } = useSubscriptionHistory();

    if (isLoading) {
        return <p className="sub-history__loading">불러오는 중...</p>;
    }

    return (
        <div className="sub-history">
            <h3 className="sub-history__title">구매 이력</h3>

            {!history || history.length === 0 ? (
                <p className="sub-history__empty">구매 이력이 없습니다.</p>
            ) : (
                <div className="sub-history__list">
                    {history.map((item) => (
                        <div key={item.subscriptionId} className="sub-history__item">
                            <div className="sub-history__item-left">
                                <span className="sub-history__car">{item.carNumber}</span>
                                <span className="sub-history__period">
                                    {formatDate(item.startDate)} ~ {formatDate(item.endDate)}
                                </span>
                            </div>
                            <div className="sub-history__item-right">
                                <span className="sub-history__price">{item.price?.toLocaleString()}원</span>
                                <span className={`sub-history-badge ${STATUS_CLASS[item.status] ?? ''}`}>
                                    {STATUS_LABEL[item.status] ?? item.status}
                                </span>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default SubscriptionHistoryList;
