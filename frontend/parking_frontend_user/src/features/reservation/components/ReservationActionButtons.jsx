import React from 'react';

const ReservationActionButtons = ({ status, onCancel, onEdit }) => {
    if (!['PENDING', 'RESERVED'].includes(status)) return null;

    return (
        <div className="res-action-buttons">
            {status === 'PENDING' && (
                <button className="btn-edit-res" type="button" onClick={onEdit}>
                    수정
                </button>
            )}
            <button className="btn-cancel-res" type="button" onClick={onCancel}>
                취소
            </button>
        </div>
    );
};

export default ReservationActionButtons;
