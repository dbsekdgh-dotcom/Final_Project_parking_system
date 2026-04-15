import React, { useState } from 'react';
import { useCreateReservation } from '../hooks/useReservation';
import './ReservationModal.css';

const PURPOSE_OPTIONS = [
    { value: 'FAMILY',   label: '가족 방문' },
    { value: 'FRIEND',   label: '지인 방문' },
    { value: 'BUSINESS', label: '업무/배달' },
    { value: 'OTHER',    label: '기타' },
];

const ReservationModal = ({ isOpen, onClose }) => {
    const { mutate: createReservation, isPending } = useCreateReservation();

    const [formData, setFormData] = useState({
        carNumber: '',
        visitStartAt: '',
        purpose: 'FAMILY',
    });

    if (!isOpen) return null;

    const handleChange = (field) => (e) =>
        setFormData((prev) => ({ ...prev, [field]: e.target.value }));

    const handleSubmit = (e) => {
        e.preventDefault();
        const payload = {
            ...formData,
            visitStartAt: formData.visitStartAt ? formData.visitStartAt + ':00' : '',
        };
        createReservation(payload, {
            onSuccess: () => onClose(),
        });
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-card" onClick={(e) => e.stopPropagation()}>

                <div className="modal-header">
                    <h2 className="modal-header__title">방문 예약 신청</h2>
                </div>

                <div className="modal-body">
                    <form onSubmit={handleSubmit}>
                        <div className="modal-field">
                            <label className="modal-label">차량 번호</label>
                            <input
                                type="text"
                                className="modal-input"
                                placeholder="예: 12가 3456"
                                value={formData.carNumber}
                                onChange={handleChange('carNumber')}
                                required
                            />
                        </div>

                        <div className="modal-field" style={{ marginTop: '14px' }}>
                            <label className="modal-label">방문 시작 일시</label>
                            <input
                                type="datetime-local"
                                className="modal-input"
                                value={formData.visitStartAt}
                                onChange={handleChange('visitStartAt')}
                                required
                            />
                        </div>

                        <div className="modal-field" style={{ marginTop: '14px' }}>
                            <label className="modal-label">방문 목적</label>
                            <select
                                className="modal-select"
                                value={formData.purpose}
                                onChange={handleChange('purpose')}
                            >
                                {PURPOSE_OPTIONS.map((opt) => (
                                    <option key={opt.value} value={opt.value}>
                                        {opt.label}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div className="modal-footer" style={{ marginTop: '22px' }}>
                            <button
                                type="button"
                                className="btn-modal-cancel"
                                onClick={onClose}
                            >
                                취소
                            </button>
                            <button
                                type="submit"
                                className="btn-modal-submit"
                                disabled={isPending}
                            >
                                {isPending ? '신청 중...' : '신청하기'}
                            </button>
                        </div>
                    </form>
                </div>

            </div>
        </div>
    );
};

export default ReservationModal;
