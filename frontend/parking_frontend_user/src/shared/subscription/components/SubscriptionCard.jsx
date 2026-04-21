import { useState } from 'react';
import RefundConfirmModal from './RefundConfirmModal';
import './SubscriptionCard.css';

const fmt = (d) => new Date(d).toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' });

export default function SubscriptionCard({ subscription }) {
    const [refundModalOpen, setRefundModalOpen] = useState(false);

    if (!subscription) {
        return (
            <div className="sub-card sub-card--empty">
                <p className="sub-card__empty-text">활성화된 정기권이 없습니다.</p>
            </div>
        );
    }

    const { carNumber, startDate, endDate, price, earnedPoint } = subscription;
    const now = new Date();
    const isUpcoming = new Date(startDate) > now;
    const daysUntilStart = Math.ceil((new Date(startDate) - now) / (1000 * 60 * 60 * 24));
    const remain = Math.max(0, Math.ceil((new Date(endDate) - now) / (1000 * 60 * 60 * 24)));

    return (
        <>
            <div className="sub-card">
                <div className="sub-card__header">
                    <span className={`sub-card__badge${isUpcoming ? ' sub-card__badge--upcoming' : ''}`}>
                        {isUpcoming ? '이용 예정' : '이용중'}
                    </span>
                    <span className="sub-card__car">{carNumber}</span>
                </div>
                <div className="sub-card__body">
                    <div className="sub-card__row">
                        <span className="sub-card__label">기간</span>
                        <span>{fmt(startDate)} ~ {fmt(endDate)}</span>
                    </div>
                    <div className="sub-card__row">
                        <span className="sub-card__label">
                            {isUpcoming ? '시작까지' : '남은 기간'}
                        </span>
                        <span className="sub-card__remain">
                            {isUpcoming ? `${daysUntilStart}일` : `${remain}일`}
                        </span>
                    </div>
                    <div className="sub-card__row">
                        <span className="sub-card__label">결제 금액</span>
                        <span>{price?.toLocaleString()}원</span>
                    </div>
                    {earnedPoint > 0 && (
                        <div className="sub-card__row">
                            <span className="sub-card__label">적립 포인트</span>
                            <span className="sub-card__point">+{earnedPoint.toLocaleString()}P</span>
                        </div>
                    )}
                </div>
                <button
                    className="sub-card__cancel-btn"
                    onClick={() => setRefundModalOpen(true)}
                >
                    환불하기
                </button>
            </div>

            {refundModalOpen && (
                <RefundConfirmModal
                    subscription={subscription}
                    onClose={() => setRefundModalOpen(false)}
                />
            )}
        </>
    );
}