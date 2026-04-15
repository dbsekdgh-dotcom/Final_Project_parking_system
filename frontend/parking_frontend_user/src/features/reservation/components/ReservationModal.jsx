import React, { useState, useEffect } from 'react';
import { useCreateReservation, useReservationPolicy } from '../hooks/useReservation';
import ReservationPolicyBox from './ReservationPolicyBox';
import './ReservationModal.css';

const PURPOSE_OPTIONS = [
    { value: 'FAMILY',   label: '가족 방문' },
    { value: 'FRIEND',   label: '지인 방문' },
    { value: 'BUSINESS', label: '업무' },
    { value: 'DELIVERY', label: '배달/택배' },
    { value: 'OTHER',    label: '기타' },
];

const HOUR_OPTIONS = Array.from({ length: 24 }, (_, i) => {
    const hh = String(i).padStart(2, '0');
    return { value: `${hh}:00`, label: `${hh}:00` };
});

const getTomorrow = () => {
    const d = new Date();
    d.setDate(d.getDate() + 1);
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
};

const ReservationModal = ({ isOpen, onClose }) => {
    const tomorrow = getTomorrow();

    const [carNumber, setCarNumber] = useState('');
    const [purpose, setPurpose]     = useState('FAMILY');
    const [dateVal, setDateVal]     = useState(tomorrow);
    const [hourVal, setHourVal]     = useState('00:00');

    const { data: policy, isLoading: isPolicyLoading } = useReservationPolicy(dateVal);
    const { mutate: createReservation, isPending } = useCreateReservation();

    useEffect(() => {
        if (isOpen) {
            const t = getTomorrow();
            setCarNumber('');
            setPurpose('FAMILY');
            setDateVal(t);
            setHourVal('00:00');
        }
    }, [isOpen]);

    if (!isOpen) return null;

    const handleSubmit = (e) => {
        e.preventDefault();

        if (policy && policy.targetDateUserCount >= policy.dailyLimitPerHousehold) {
            alert('일일 예약 가능 횟수를 초과했습니다.');
            return;
        }

        createReservation(
            { carNumber, purpose, visitStartAt: `${dateVal}T${hourVal}:00` },
            { onSuccess: () => onClose() }
        );
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-card" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <h2 className="modal-header__title">방문 예약 신청</h2>
                </div>

                <div className="modal-body">
                    <ReservationPolicyBox policy={policy} isLoading={isPolicyLoading} date={dateVal} />

                    <form onSubmit={handleSubmit}>
                        {/* 차량번호 */}
                        <div className="modal-field">
                            <label className="modal-label">차량 번호</label>
                            <input
                                type="text"
                                className="modal-input"
                                placeholder="예: 12가 3456"
                                value={carNumber}
                                onChange={(e) => setCarNumber(e.target.value)}
                                required
                            />
                        </div>

                        {/* 방문 시작 일시 */}
                        <div className="modal-field" style={{ marginTop: '14px' }}>
                            <label className="modal-label">방문 시작 일시</label>
                            <div className="modal-datetime-row">
                                <input
                                    type="date"
                                    className="modal-input"
                                    value={dateVal}
                                    min={tomorrow}
                                    onChange={(e) => setDateVal(e.target.value)}
                                    required
                                />
                                <select
                                    className="modal-select modal-hour-select"
                                    value={hourVal}
                                    onChange={(e) => setHourVal(e.target.value)}
                                >
                                    {HOUR_OPTIONS.map((opt) => (
                                        <option key={opt.value} value={opt.value}>{opt.label}</option>
                                    ))}
                                </select>
                            </div>
                        </div>

                        {/* 방문 목적 */}
                        <div className="modal-field" style={{ marginTop: '14px' }}>
                            <label className="modal-label">방문 목적</label>
                            <select
                                className="modal-select"
                                value={purpose}
                                onChange={(e) => setPurpose(e.target.value)}
                            >
                                {PURPOSE_OPTIONS.map((opt) => (
                                    <option key={opt.value} value={opt.value}>
                                        {opt.label}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div className="modal-footer" style={{ marginTop: '22px' }}>
                            <button type="button" className="btn-modal-cancel" onClick={onClose}>
                                취소
                            </button>
                            <button
                                type="submit"
                                className="btn-modal-submit"
                                disabled={isPending || (policy && policy.targetDateTotalCount >= policy.totalDailyLimit)}
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
