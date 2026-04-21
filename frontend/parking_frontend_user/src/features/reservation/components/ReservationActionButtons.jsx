import React from 'react';

const isTodayOrPast = (dateStr) => {
    if (!dateStr) return false;
    const visit = new Date(dateStr);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    visit.setHours(0, 0, 0, 0);
    return visit <= today;
};

const ReservationActionButtons = ({ status, visitStartAt, onCancel, onEdit }) => {
    if (!['PENDING', 'RESERVED'].includes(status)) return null;

    const showCancel = !isTodayOrPast(visitStartAt);

    return (
        <div className="res-action-buttons">
            {status === 'PENDING' && (
                <button className="btn-edit-res" type="button" onClick={onEdit}>
                    수정
                </button>
            )}
            {showCancel && (
                <button className="btn-cancel-res" type="button" onClick={onCancel}>
                    취소
                </button>
            )}
        </div>
    );
};

export default ReservationActionButtons;
