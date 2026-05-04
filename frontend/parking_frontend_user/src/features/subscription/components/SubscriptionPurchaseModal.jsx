import { useState } from 'react';
import { useSubscriptionPolicy, useMyPoint } from '../hooks/useSubscription';
import SubscriptionPeriodInfo from './SubscriptionPeriodInfo';
import SubscriptionPriceInfo from './SubscriptionPriceInfo';
import PointInput from './PointInput';
import TossPaymentWidget from './TossPaymentWidget';
import './SubscriptionPurchaseModal.css';

export default function SubscriptionPurchaseModal({ vehicle, activeSubscriptions = [], onClose, initialDate = '' }) {
    const [selectedDate, setSelectedDate] = useState(initialDate);
    const [overlapError, setOverlapError] = useState('');
    const [usedPoint, setUsedPoint] = useState(0);
    const [showToss, setShowToss] = useState(false);

    const startDate = selectedDate ? new Date(selectedDate) : null;
    const { data: policy } = useSubscriptionPolicy(startDate);
    const { data: myPoint = 0 } = useMyPoint();

    const price = policy?.price ?? 0;
    const days = policy?.durationDays ?? 30;
    const remaining = policy?.remainCount ?? 0;
    const paidAmount = Math.max(0, price - usedPoint);

    const isOverlapping = (dateStr) => {
        if (!dateStr || activeSubscriptions.length === 0) return false;
        const sel = new Date(dateStr);
        const selEnd = new Date(sel);
        selEnd.setDate(selEnd.getDate() + (days || 30));
        return activeSubscriptions.some(sub => {
            const subStart = new Date(sub.startDate);
            const subEnd = new Date(sub.endDate);
            return sel < subEnd && selEnd > subStart;
        });
    };

    const handleGoToss = () => {
        window.__subOrderId = crypto.randomUUID();
        localStorage.setItem('sub_vehicleId', vehicle.vehicleId);
        localStorage.setItem('sub_usedPoint', usedPoint);
        localStorage.setItem('sub_paidAmount', paidAmount);
        localStorage.setItem('sub_orderId', window.__subOrderId);
        localStorage.setItem('sub_startDate', startDate?.toISOString() ?? '');
        setShowToss(true);
    };

    const handlePointOnly = () => {
        localStorage.setItem('sub_vehicleId', vehicle.vehicleId);
        localStorage.setItem('sub_usedPoint', usedPoint);
        localStorage.setItem('sub_paidAmount', 0);
        localStorage.setItem('sub_orderId', '');
        localStorage.setItem('sub_paymentKey', '');
        localStorage.setItem('sub_startDate', startDate?.toISOString() ?? '');
        window.location.href = '/subscription/success?pointOnly=true';
    };

    const today = new Date().toISOString().split('T')[0];

    return (
        <div className="modal-overlay">
            <div className="sub-modal-card" onClick={e => e.stopPropagation()}>
                <div className="sub-modal-header">
                    <h2 className="sub-modal-title">정기권 구매</h2>
                    <button className="sub-modal-close" onClick={onClose}>✕</button>
                </div>

                <div className="sub-modal-body">
                    {/* 차량 정보 */}
                    <div className="sub-modal-section">
                        <label className="sub-modal-label">차량</label>
                        <div className="sub-modal-period">{vehicle?.carNumber} ({vehicle?.vehicleName})</div>
                    </div>

                    {/* 시작일 선택 */}
                    <div className="sub-modal-section">
                        <label className="sub-modal-label">시작일 선택</label>
                        <input
                            type="date"
                            className={`sub-modal-select${overlapError ? ' sub-modal-select--error' : ''}`}
                            min={today}
                            value={selectedDate}
                            onChange={e => {
                                const val = e.target.value;
                                if (isOverlapping(val)) {
                                    setOverlapError('보유 중인 정기권 기간과 겹칩니다. 다른 날짜를 선택해주세요.');
                                    setSelectedDate('');
                                } else {
                                    setOverlapError('');
                                    setSelectedDate(val);
                                }
                                setUsedPoint(0);
                                setShowToss(false);
                            }}
                        />
                        {overlapError && <p className="sub-modal-error">{overlapError}</p>}
                    </div>

                    {/* 날짜 선택 후 정책 표시 */}
                    {selectedDate && policy && (
                        <>
                            <SubscriptionPeriodInfo days={days} startDate={startDate} />
                            <SubscriptionPriceInfo price={price} remaining={remaining} />

                            {!showToss && (
                                <>
                                    <PointInput myPoint={myPoint} price={price} onApply={setUsedPoint} />

                                    <div className="sub-modal-total">
                                        {usedPoint > 0 && (
                                            <div className="sub-modal-price-row sub-modal-discount">
                                                <span>포인트 할인</span>
                                                <span>- {usedPoint.toLocaleString()}P</span>
                                            </div>
                                        )}
                                        <div className="sub-modal-price-row sub-modal-final">
                                            <span>최종 결제금액</span>
                                            <span>{paidAmount.toLocaleString()}원</span>
                                        </div>
                                    </div>

                                    <div className="sub-modal-footer">
                                        {remaining <= 0 ? (
                                            <button className="sub-modal-btn-primary" disabled style={{ opacity: 0.45, cursor: 'not-allowed' }}>
                                                잔여 수량 없음
                                            </button>
                                        ) : paidAmount === 0 ? (
                                            <button className="sub-modal-btn-primary" onClick={handlePointOnly}>포인트로 구매하기</button>
                                        ) : (
                                            <button className="sub-modal-btn-primary" onClick={handleGoToss}>결제하기</button>
                                        )}
                                    </div>
                                </>
                            )}

                            {showToss && (
                                <TossPaymentWidget
                                    paidAmount={paidAmount}
                                    days={days}
                                    orderId={window.__subOrderId}
                                    onReady={() => {}}
                                />
                            )}
                        </>
                    )}

                    {selectedDate && !policy && (
                        <p style={{ color: '#888', fontSize: '0.9rem' }}>정책 정보를 불러오는 중...</p>
                    )}

                    {!selectedDate && (
                        <p style={{ color: '#aaa', fontSize: '0.88rem' }}>시작일을 선택하면 정기권 정보가 표시됩니다.</p>
                    )}
                </div>
            </div>
        </div>
    );
}