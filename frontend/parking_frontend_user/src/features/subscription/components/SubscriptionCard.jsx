import React, { useState } from 'react';
import { useMySubscription, useRefundSubscription } from '../hooks/useSubscription';
import SubscriptionPurchaseModal from './SubscriptionPurchaseModal';
import Swal from 'sweetalert2';
import './SubscriptionCard.css';

const STATUS_LABEL = {
    ACTIVE: '이용 중',
    EXPIRED: '만료',
    REFUNDED: '환불 완료',
    CANCELLED: '취소됨',
};

const STATUS_CLASS = {
    ACTIVE: 'sub-badge--active',
    EXPIRED: 'sub-badge--expired',
    REFUNDED: 'sub-badge--refunded',
    CANCELLED: 'sub-badge--cancelled',
};

const formatDate = (dateStr) => {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' });
};

const SubscriptionCard = () => {
    const { data: subscription, isLoading } = useMySubscription();
    const { mutate: refund, isLoading: isRefunding } = useRefundSubscription();
    const [isPurchaseOpen, setIsPurchaseOpen] = useState(false);

    const handleRefund = () => {
        Swal.fire({
            title: '정기권 환불 신청',
            html: `
                <p style="margin:0 0 8px;color:#444;">정기권을 환불하시겠습니까?</p>
                <p style="margin:0;font-size:0.85rem;color:#888;">
                  시작 전이면 전액, 시작 후면 남은 일수 기준으로 일할 환불됩니다.
                </p>
            `,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: '환불 신청',
            cancelButtonText: '취소',
            confirmButtonColor: '#d33',
            cancelButtonColor: '#6f708b',
        }).then((result) => {
            if (result.isConfirmed) {
                refund(subscription.subscriptionId);
            }
        });
    };

    if (isLoading) {
        return (
            <div className="sub-card">
                <p className="sub-card__loading">불러오는 중...</p>
            </div>
        );
    }

    return (
        <>
            <div className="sub-card">
                <h3 className="sub-card__title">내 정기권</h3>

                {!subscription ? (
                    <div className="sub-card__empty">
                        <p className="sub-card__empty-text">현재 이용 중인 정기권이 없습니다.</p>
                        <button className="btn-sub btn-sub--blue" onClick={() => setIsPurchaseOpen(true)}>
                            정기권 구매
                        </button>
                    </div>
                ) : (
                    <>
                        <div className="sub-card__body">
                            <div className="sub-card__row">
                                <span className="sub-card__label">차량 번호</span>
                                <span className="sub-card__value">{subscription.carNumber}</span>
                            </div>
                            <div className="sub-card__row">
                                <span className="sub-card__label">이용 기간</span>
                                <span className="sub-card__value">
                                    {formatDate(subscription.startDate)} ~ {formatDate(subscription.endDate)}
                                </span>
                            </div>
                            <div className="sub-card__row">
                                <span className="sub-card__label">남은 일수</span>
                                <span className="sub-card__value sub-card__dday">{subscription.remainingDays}일</span>
                            </div>
                            <div className="sub-card__row">
                                <span className="sub-card__label">결제 금액</span>
                                <span className="sub-card__value">{subscription.price?.toLocaleString()}원</span>
                            </div>
                        </div>

                        <div className="sub-card__footer">
                            <span className={`sub-badge ${STATUS_CLASS[subscription.status] ?? ''}`}>
                                {STATUS_LABEL[subscription.status] ?? subscription.status}
                            </span>
                            {subscription.status === 'ACTIVE' && (
                                <button
                                    className="btn-sub btn-sub--red"
                                    onClick={handleRefund}
                                    disabled={isRefunding}
                                >
                                    {isRefunding ? '처리 중...' : '환불 신청'}
                                </button>
                            )}
                        </div>
                    </>
                )}
            </div>

            <SubscriptionPurchaseModal
                isOpen={isPurchaseOpen}
                onClose={() => setIsPurchaseOpen(false)}
            />
        </>
    );
};

export default SubscriptionCard;
