import { useEffect, useRef } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { purchaseSubscription } from '../api/subscriptionApi';
import Swal from 'sweetalert2';

export default function SubscriptionSuccessPage() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const processed = useRef(false);

    useEffect(() => {
        if (processed.current) return;
        processed.current = true;

        async function confirm() {
            Swal.fire({
                title: '결제 처리 중...',
                text: '잠시만 기다려 주세요.',
                allowOutsideClick: false,
                allowEscapeKey: false,
                didOpen: () => Swal.showLoading(),
            });

            const vehicleId  = Number(localStorage.getItem('sub_vehicleId'));
            const usedPoint  = Number(localStorage.getItem('sub_usedPoint'));
            const paidAmount = Number(localStorage.getItem('sub_paidAmount'));
            const orderId    = localStorage.getItem('sub_orderId') || searchParams.get('orderId');
            const paymentKey = searchParams.get('paymentKey') || '';
            const pointOnly  = searchParams.get('pointOnly') === 'true';
            const startDateStr = localStorage.getItem('sub_startDate');

            try {
                await purchaseSubscription({
                    vehicleId,
                    usedPoint,
                    paidAmount: pointOnly ? 0 : paidAmount,
                    paymentKey: pointOnly ? null : paymentKey,
                    orderId: pointOnly ? null : orderId,
                    startDate: startDateStr || null,
                });

                ['sub_vehicleId', 'sub_usedPoint', 'sub_paidAmount', 'sub_orderId', 'sub_startDate']
                    .forEach(k => localStorage.removeItem(k));

                await Swal.fire({
                    icon: 'success',
                    title: '정기권 구매 완료!',
                    text: '정기권이 정상적으로 등록되었습니다.',
                    confirmButtonText: '확인',
                    confirmButtonColor: '#4f7af8',
                });
            } catch (e) {
                await Swal.fire({
                    icon: 'error',
                    title: '구매 실패',
                    text: e?.response?.data?.message || '구매 처리 중 오류가 발생했습니다.',
                    confirmButtonText: '확인',
                    confirmButtonColor: '#d33',
                });
            }
            navigate('/subscription', { replace: true });
        }

        confirm();
    }, []);

    return null;
}
