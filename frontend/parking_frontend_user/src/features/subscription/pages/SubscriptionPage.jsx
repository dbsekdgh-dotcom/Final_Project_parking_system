import React, { useEffect, useRef } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useConfirmSubscription } from '../hooks/useSubscription';
import SubscriptionCard from '../components/SubscriptionCard';
import SubscriptionHistoryList from '../components/SubscriptionHistoryList';
import Swal from 'sweetalert2';
import './SubscriptionPage.css';

const SubscriptionPage = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const { mutate: confirm } = useConfirmSubscription();
    const processed = useRef(false);

    // Toss 결제 성공 리다이렉트 처리
    useEffect(() => {
        if (processed.current) return;

        const paymentKey = searchParams.get('paymentKey');
        const orderId = searchParams.get('orderId');
        const amount = searchParams.get('amount');
        const isFail = searchParams.get('fail');

        if (isFail) {
            Swal.fire({ icon: 'error', title: '결제 실패', text: '결제가 취소되었거나 실패했습니다.', confirmButtonColor: '#d33' });
            setSearchParams({});
            return;
        }

        if (paymentKey && orderId && amount) {
            const pending = localStorage.getItem('sub_pending');
            if (!pending) {
                Swal.fire({ icon: 'error', title: '오류', text: '결제 정보를 찾을 수 없습니다. 다시 시도해주세요.', confirmButtonColor: '#d33' });
                setSearchParams({});
                return;
            }

            const { carNumber, startDate, endDate } = JSON.parse(pending);
            processed.current = true;
            localStorage.removeItem('sub_pending');
            setSearchParams({});

            confirm({
                paymentKey,
                orderId,
                amount: Number(amount),
                carNumber,
                startDate,
                endDate,
            });
        }
    }, []);

    return (
        <div className="sub-page">
            <h2 className="sub-page__title">정기권</h2>
            <SubscriptionCard />
            <SubscriptionHistoryList />
        </div>
    );
};

export default SubscriptionPage;
