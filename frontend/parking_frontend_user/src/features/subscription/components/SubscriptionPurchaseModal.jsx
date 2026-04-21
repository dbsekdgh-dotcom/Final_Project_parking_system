import React, { useState, useRef, useEffect } from 'react';
import { useMyVehicle } from '../../vehicle/hooks/useVehicle';
import { useReadySubscription, useSubscriptionPolicy } from '../hooks/useSubscription';
import { loadTossPayments, ANONYMOUS } from '@tosspayments/tosspayments-sdk';
import Swal from 'sweetalert2';
import './SubscriptionPurchaseModal.css';

const TOSS_CLIENT_KEY = import.meta.env.VITE_TOSS_CLIENT_KEY;

const SubscriptionPurchaseModal = ({ isOpen, onClose }) => {
    const { data: vehicle, isLoading: isVehicleLoading } = useMyVehicle();
    const [startDate, setStartDate] = useState('');
    const [step, setStep] = useState(1);
    const [widgetsReady, setWidgetsReady] = useState(false);
    const widgetsRef = useRef(null);
    const pendingReadyData = useRef(null);

    const { mutate: ready, isLoading } = useReadySubscription();

    const startDateTime = startDate ? `${startDate}T00:00:00` : null;
    const { data: policy, isFetching: isPolicyFetching } = useSubscriptionPolicy(startDateTime);

    // 모달 닫힐 때 상태 초기화
    useEffect(() => {
        if (!isOpen) {
            setStep(1);
            setStartDate('');
            setWidgetsReady(false);
            widgetsRef.current = null;
            pendingReadyData.current = null;
        }
    }, [isOpen]);

    // step 2 진입 시 Toss 위젯 초기화 및 렌더링
    useEffect(() => {
        if (step !== 2 || !pendingReadyData.current) return;

        setWidgetsReady(false);
        let cancelled = false;

        (async () => {
            try {
                const customerEmail = localStorage.getItem('userEmail') || undefined;
                const tossPayments = await loadTossPayments(TOSS_CLIENT_KEY);
                const widgets = tossPayments.widgets({ customerKey: customerEmail || ANONYMOUS });
                await widgets.setAmount({ currency: 'KRW', value: Number(pendingReadyData.current.amount) });

                await Promise.all([
                    widgets.renderPaymentMethods({ selector: '#toss-payment-method', variantKey: 'DEFAULT' }),
                    widgets.renderAgreement({ selector: '#toss-agreement', variantKey: 'AGREEMENT' }),
                ]);

                if (!cancelled) {
                    widgetsRef.current = widgets;
                    setWidgetsReady(true);
                }
            } catch (err) {
                if (!cancelled) {
                    Swal.fire({ icon: 'error', title: '결제 초기화 실패', text: err.message, confirmButtonColor: '#d33' });
                    setStep(1);
                }
            }
        })();

        return () => { cancelled = true; };
    }, [step]);

    if (!isOpen) return null;

    const today = new Date().toISOString().split('T')[0];

    if (!isVehicleLoading && (!vehicle || vehicle.status !== 'ACTIVE')) {
        return (
            <div className="modal-overlay">
                <div className="modal-card">
                    <div className="modal-header">
                        <h2 className="modal-header__title">정기권 구매</h2>
                        <button className="modal-close-btn" onClick={onClose}>&times;</button>
                    </div>
                    <div className="modal-body">
                        <div className="sub-purchase-no-vehicle">
                            <p>승인된 차량이 없습니다.</p>
                            <p className="sub-purchase-no-vehicle__sub">차량 등록 승인 후 정기권을 구매할 수 있습니다.</p>
                        </div>
                    </div>
                    <div className="modal-footer">
                        <button className="btn-modal-cancel" onClick={onClose}>닫기</button>
                    </div>
                </div>
            </div>
        );
    }

    const handleReady = () => {
        if (!startDate) {
            Swal.fire({ icon: 'warning', title: '입력 확인', text: '시작일을 선택해주세요.', confirmButtonColor: '#3085d6' });
            return;
        }
        if (!policy) {
            Swal.fire({ icon: 'warning', title: '정책 확인 중', text: '잠시 후 다시 시도해주세요.', confirmButtonColor: '#3085d6' });
            return;
        }
        if (policy.remainCount <= 0) {
            Swal.fire({ icon: 'error', title: '잔여 수량 없음', text: '선택한 날짜의 정기권이 모두 판매되었습니다.', confirmButtonColor: '#d33' });
            return;
        }

        ready(
            { carNumber: vehicle.carNumber, startDate: startDateTime, amount: policy.price },
            {
                onSuccess: (data) => {
                    localStorage.setItem('sub_pending', JSON.stringify({
                        carNumber: data.carNumber,
                        startDate: data.startDate,
                        endDate: data.endDate,
                    }));
                    pendingReadyData.current = data;
                    setStep(2);
                },
            }
        );
    };

    const handlePay = async () => {
        if (!widgetsReady || !widgetsRef.current) {
            Swal.fire({ icon: 'warning', title: '결제 준비 중', text: '잠시 후 다시 시도해주세요.', confirmButtonColor: '#3085d6' });
            return;
        }
        try {
            const data = pendingReadyData.current;
            await widgetsRef.current.requestPayment({
                orderId: data.orderId,
                orderName: data.orderName,
                customerName: localStorage.getItem('userName') || '사용자',
                customerEmail: localStorage.getItem('userEmail') || undefined,
                successUrl: `${window.location.origin}/subscription`,
                failUrl: `${window.location.origin}/subscription?fail=1`,
            });
        } catch (err) {
            localStorage.removeItem('sub_pending');
            Swal.fire({
                icon: 'error',
                title: '결제창 오류',
                text: err.message || '결제창을 열 수 없습니다. 다시 시도해주세요.',
                confirmButtonColor: '#d33',
            });
        }
    };

    const isPolicyReady = startDate && !isPolicyFetching && policy;

    return (
        <div className="modal-overlay">
            <div className="modal-card">
                <div className="modal-header">
                    <h2 className="modal-header__title">정기권 구매</h2>
                    <button className="modal-close-btn" onClick={onClose}>&times;</button>
                </div>

                <div className="modal-body">
                    {step === 1 ? (
                        <>
                            <div className="sub-purchase-vehicle">
                                <span className="sub-purchase-vehicle__label">등록 차량</span>
                                <span className="sub-purchase-vehicle__number">{vehicle?.carNumber}</span>
                            </div>

                            <div className="modal-field">
                                <label className="modal-label">시작일</label>
                                <input
                                    className="modal-input"
                                    type="date"
                                    value={startDate}
                                    min={today}
                                    onChange={(e) => setStartDate(e.target.value)}
                                />
                            </div>

                            <div className="sub-purchase-info">
                                {isPolicyFetching ? (
                                    <p className="sub-purchase-info__loading">정책 정보 확인 중...</p>
                                ) : (
                                    <>
                                        <div className="sub-purchase-info__row">
                                            <span className="sub-purchase-info__label">이용 기간</span>
                                            <span className="sub-purchase-info__value">
                                                {policy ? `${policy.durationDays}일` : '시작일을 선택해주세요'}
                                            </span>
                                        </div>
                                        <div className="sub-purchase-info__row">
                                            <span className="sub-purchase-info__label">잔여 수량</span>
                                            <span className={`sub-purchase-info__value ${policy?.remainCount <= 0 ? 'sub-purchase-info__soldout' : ''}`}>
                                                {policy ? `${policy.remainCount}개` : '-'}
                                            </span>
                                        </div>
                                        <div className="sub-purchase-info__row">
                                            <span className="sub-purchase-info__label">결제 금액</span>
                                            <span className="sub-purchase-info__value sub-purchase-info__price">
                                                {policy ? `${policy.price.toLocaleString()}원` : '-'}
                                            </span>
                                        </div>
                                    </>
                                )}
                            </div>
                        </>
                    ) : (
                        <div className="sub-purchase-payment-widget">
                            {!widgetsReady && (
                                <p className="sub-purchase-info__loading">결제 수단 로딩 중...</p>
                            )}
                            <div id="toss-payment-method" />
                            <div id="toss-agreement" />
                        </div>
                    )}
                </div>

                <div className="modal-footer">
                    {step === 1 ? (
                        <>
                            <button
                                className="btn-modal-submit"
                                onClick={handleReady}
                                disabled={isLoading || isPolicyFetching || !isPolicyReady}
                            >
                                {isLoading ? '확인 중...' : '다음'}
                            </button>
                            <button className="btn-modal-cancel" onClick={onClose}>취소</button>
                        </>
                    ) : (
                        <>
                            <button
                                className="btn-modal-submit"
                                onClick={handlePay}
                                disabled={!widgetsReady}
                            >
                                {widgetsReady ? '결제하기' : '로딩 중...'}
                            </button>
                            <button
                                className="btn-modal-cancel"
                                onClick={() => { setStep(1); pendingReadyData.current = null; sessionStorage.removeItem('sub_pending'); }}
                            >
                                이전
                            </button>
                        </>
                    )}
                </div>
            </div>
        </div>
    );
};

export default SubscriptionPurchaseModal;
