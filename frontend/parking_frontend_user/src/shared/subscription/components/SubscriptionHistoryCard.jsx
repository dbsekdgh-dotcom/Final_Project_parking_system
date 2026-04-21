import { useState } from 'react';
import './SubscriptionHistoryCard.css';

const PAGE_SIZE = 5;

const fmt = (d) => new Date(d).toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' });

const getStatusLabel = (s) => {
    if (s.status === 'ACTIVE') {
        return new Date(s.startDate) > new Date() ? '이용 예정' : '이용중';
    }
    return { EXPIRED: '만료', CANCELLED: '취소', REFUNDED: '환불' }[s.status] ?? s.status;
};

export default function SubscriptionHistoryCard({ history }) {
    const [page, setPage] = useState(0);

    if (!history || history.length === 0) return null;

    const totalPages = Math.ceil(history.length / PAGE_SIZE);
    const paged = history.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE);

    return (
        <div className="sub-history-card">
            <h4 className="sub-history-card__title">구매 이력</h4>
            <div className="sub-history-card__list">
                {paged.map(s => (
                    <div key={s.subscriptionId} className="sub-history-card__item">
                        <div className="sub-history-card__row">
                            <span className={`sub-history-card__badge sub-history-card__badge--${s.status === 'ACTIVE' && new Date(s.startDate) > new Date() ? 'upcoming' : s.status.toLowerCase()}`}>
                                {getStatusLabel(s)}
                            </span>
                            <span className="sub-history-card__car">{s.carNumber}</span>
                        </div>
                        <div className="sub-history-card__row sub-history-card__row--detail">
                            <span className="sub-history-card__label">기간</span>
                            <span>{fmt(s.startDate)} ~ {fmt(s.endDate)}</span>
                        </div>
                        <div className="sub-history-card__row sub-history-card__row--detail">
                            <span className="sub-history-card__label">결제 금액</span>
                            <span>{s.price?.toLocaleString()}원</span>
                        </div>
                        {s.earnedPoint > 0 && (
                            <div className="sub-history-card__row sub-history-card__row--detail">
                                <span className="sub-history-card__label">적립 포인트</span>
                                <span className="sub-history-card__point">+{s.earnedPoint.toLocaleString()}P</span>
                            </div>
                        )}
                    </div>
                ))}
            </div>

            {totalPages > 1 && (
                <div className="sub-history-card__pagination">
                    <button
                        className="sub-history-card__page-btn"
                        onClick={() => setPage(p => p - 1)}
                        disabled={page === 0}
                    >
                        이전
                    </button>

                    {Array.from({ length: totalPages }, (_, i) => (
                        <button
                            key={i}
                            className={`sub-history-card__page-btn ${i === page ? 'sub-history-card__page-btn--active' : ''}`}
                            onClick={() => setPage(i)}
                        >
                            {i + 1}
                        </button>
                    ))}

                    <button
                        className="sub-history-card__page-btn"
                        onClick={() => setPage(p => p + 1)}
                        disabled={page === totalPages - 1}
                    >
                        다음
                    </button>
                </div>
            )}
        </div>
    );
}
