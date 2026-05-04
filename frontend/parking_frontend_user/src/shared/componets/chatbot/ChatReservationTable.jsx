import React from 'react';
import './ChatReservationTable.css';

const STATUS_MAP = {
    PENDING:   { text: '승인대기',    cls: 'crt-status--pending' },
    RESERVED:  { text: '예약완료',    cls: 'crt-status--reserved' },
    CANCELLED: { text: '취소됨',      cls: 'crt-status--cancelled' },
    NO_SHOW:   { text: '미방문',      cls: 'crt-status--noshow' },
    ENTERED:   { text: '주차중',      cls: 'crt-status--entered' },
    COMPLETED: { text: '이용완료',    cls: 'crt-status--completed' },
    REJECTED:  { text: '거절됨',      cls: 'crt-status--cancelled' },
};

const PURPOSE_MAP = {
    FAMILY:   '가족',
    FRIEND:   '친구',
    BUSINESS: '업무',
    DELIVERY: '배달',
    OTHER:    '기타',
};

const formatDate = (dateStr) => {
    if (!dateStr) return '-';
    const d = new Date(dateStr);
    const month = d.getMonth() + 1;
    const day = d.getDate();
    const hour = d.getHours();
    const ampm = hour < 12 ? '오전' : '오후';
    const h12 = hour === 0 ? 12 : hour > 12 ? hour - 12 : hour;
    return `${month}/${day} ${ampm} ${h12}시`;
};

const ChatReservationTable = ({ reservations }) => {
    if (!reservations || reservations.length === 0) {
        return <p className="crt-empty">예약 내역이 없습니다.</p>;
    }

    return (
        <div className="crt-wrap">
            {reservations.map((r, idx) => {
                const status = STATUS_MAP[r.status] ?? { text: r.status, cls: '' };
                const isCancelled = r.status === 'CANCELLED' || r.status === 'NO_SHOW' || r.status === 'REJECTED';
                return (
                    <div key={r.reservationId ?? idx} className={`crt-card${isCancelled ? ' crt-card--dim' : ''}`}>
                        <div className="crt-card__top">
                            <span className="crt-car">{r.carNumber}</span>
                            <span className={`crt-status ${status.cls}`}>{status.text}</span>
                        </div>
                        <div className="crt-card__bottom">
                            <span className="crt-date">{formatDate(r.visitStartAt)}</span>
                            <span className="crt-purpose">{PURPOSE_MAP[r.purpose] ?? r.purpose}</span>
                        </div>
                    </div>
                );
            })}
        </div>
    );
};

export default ChatReservationTable;
