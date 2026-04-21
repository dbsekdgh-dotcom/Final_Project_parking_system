import { useState } from 'react';
import Swal from 'sweetalert2';
import { useMyPoint, useCancelSubscription } from '../hooks/useSubscription';
import './RefundConfirmModal.css';

const floor = (n) => Math.floor(n);

export default function RefundConfirmModal({ subscription, onClose }) {
    const { data: currentPoint = 0 } = useMyPoint();
    const { mutate: cancel, isPending } = useCancelSubscription();
    const [agreed, setAgreed] = useState(false);

    const now = new Date();
    const start = new Date(subscription.startDate);
    const end = new Date(subscription.endDate);
    const totalDays = Math.floor((end - start) / (1000 * 60 * 60 * 24));
    const remainDays = Math.max(0, Math.floor((end - now) / (1000 * 60 * 60 * 24)));
    const ratio = totalDays > 0 ? remainDays / totalDays : 0;

    const paidAmount  = subscription.paidAmount ?? subscription.price;
    const usedPoint   = subscription.usedPoint ?? 0;
    const earnedPoint = subscription.earnedPoint ?? 0;

    // 현금 결제분 환불 (비율 적용)
    const rawCashRefund = floor(paidAmount * ratio);

    // 적립 포인트 회수 (현금 결제 시에만)
    const revokePoint   = floor(earnedPoint * ratio);
    const actualRevoke  = Math.min(revokePoint, currentPoint);
    const shortfall     = revokePoint - actualRevoke;

    // 포인트 결제분 반환
    const pointRefund = floor(usedPoint * ratio);

    // 최종 현금 환불액
    const finalCash = Math.max(0, rawCashRefund - shortfall);

    const handleConfirm = () => {
        Swal.fire({
            title: '정말로 환불하시겠습니까?',
            text: '환불 후에는 취소할 수 없습니다.',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: '환불',
            cancelButtonText: '취소',
            confirmButtonColor: '#e53e3e',
        }).then(result => {
            if (result.isConfirmed) {
                cancel(subscription.subscriptionId, { onSettled: onClose });
            }
        });
    };

    return (
        <div className="refund-modal__overlay" onClick={onClose}>
            <div className="refund-modal" onClick={e => e.stopPropagation()}>
                <h3 className="refund-modal__title">정기권 환불 안내</h3>

                {/* 환불 예상 금액 */}
                <div className="refund-modal__summary">
                    <div className="refund-modal__row">
                        <span className="refund-modal__label">잔여 일수</span>
                        <span className="refund-modal__value">{remainDays}일 / {totalDays}일</span>
                    </div>
                    <div className="refund-modal__row">
                        <span className="refund-modal__label">예정 환불금</span>
                        <span className="refund-modal__value">{rawCashRefund.toLocaleString()}원</span>
                    </div>
                    {revokePoint > 0 && (
                        <div className="refund-modal__row refund-modal__row--deduct">
                            <span className="refund-modal__label">혜택 정산</span>
                            <span className="refund-modal__value">
                                -{shortfall.toLocaleString()}원
                                {shortfall > 0 && <span className="refund-modal__hint"> (포인트 잔고 부족)</span>}
                                {shortfall === 0 && <span className="refund-modal__hint"> (포인트 차감)</span>}
                            </span>
                        </div>
                    )}
                    {pointRefund > 0 && (
                        <div className="refund-modal__row refund-modal__row--point">
                            <span className="refund-modal__label">포인트 반환</span>
                            <span className="refund-modal__value">+{pointRefund.toLocaleString()}P</span>
                        </div>
                    )}
                    <div className="refund-modal__divider" />
                    <div className="refund-modal__row refund-modal__row--total">
                        <span className="refund-modal__label">최종 입금액</span>
                        <span className="refund-modal__value refund-modal__final">{finalCash.toLocaleString()}원</span>
                    </div>
                </div>

                {/* 약관 */}
                <div className="refund-modal__terms">
                    <p className="refund-modal__terms-title">⚠️ 환불 규정 안내</p>
                    <ul className="refund-modal__terms-list">
                        <li>정기권 환불은 잔여 일수에 비례하여 일할 계산됩니다.</li>
                        <li>구매 시 지급된 적립 포인트는 중도 해지 시 환불 비율에 따라 회수됩니다.</li>
                        <li>이미 포인트를 사용하여 회수가 불가한 경우, 해당 금액만큼 현금 환불액에서 공제됩니다.</li>
                        <li>환불 금액 계산 시 원 단위 미만은 절사합니다.</li>
                    </ul>
                </div>

                <label className="refund-modal__agree">
                    <input
                        type="checkbox"
                        checked={agreed}
                        onChange={e => setAgreed(e.target.checked)}
                    />
                    위 환불 규정을 확인하였으며, 이에 동의합니다.
                </label>

                <div className="refund-modal__actions">
                    <button className="refund-modal__btn refund-modal__btn--cancel" onClick={onClose}>
                        취소
                    </button>
                    <button
                        className="refund-modal__btn refund-modal__btn--confirm"
                        onClick={handleConfirm}
                        disabled={!agreed || isPending}
                    >
                        {isPending ? '처리중...' : '환불하기'}
                    </button>
                </div>
            </div>
        </div>
    );
}
