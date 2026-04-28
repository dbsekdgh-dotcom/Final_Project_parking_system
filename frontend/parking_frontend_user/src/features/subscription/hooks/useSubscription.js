import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getMyPointForSubscription, getSubscriptionPolicy, getMySubscriptions, purchaseSubscription, cancelSubscription } from '../api/subscriptionApi';
import Swal from 'sweetalert2';

export const useMyPoint = () => {
    return useQuery({
        queryKey: ['subscriptionMyPoint'],
        queryFn: getMyPointForSubscription,
    });
};

export const useSubscriptionPolicy = (startDate = null) => {
    return useQuery({
        queryKey: ['subscriptionPolicy', startDate?.toISOString()],
        queryFn: () => getSubscriptionPolicy(startDate),
        enabled: !!startDate,
    });
};

export const useMySubscriptions = () => {
    return useQuery({
        queryKey: ['subscriptions'],
        queryFn: getMySubscriptions,
    });
};

export const usePurchaseSubscription = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: purchaseSubscription,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['subscriptions'] });
            queryClient.invalidateQueries({ queryKey: ['subscriptionMyPoint'] });
        },
        onError: (error) => {
            Swal.fire({
                icon: 'error',
                title: '구매 실패',
                text: error.response?.data?.message || '정기권 구매 중 오류가 발생했습니다.',
                confirmButtonColor: '#d33',
            });
        },
    });
};

export const useCancelSubscription = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: cancelSubscription,
        onSuccess: (data) => {
            queryClient.invalidateQueries({ queryKey: ['subscriptions'] });
            queryClient.invalidateQueries({ queryKey: ['subscriptionMyPoint'] });

            const lines = [`현금 환불: ${data.cashRefundAmount.toLocaleString()}원`];
            if (data.pointRefundAmount > 0)
                lines.push(`포인트 반환: ${data.pointRefundAmount.toLocaleString()}P`);
            if (data.revokedPoint > 0)
                lines.push(`적립 포인트 회수: ${data.revokedPoint.toLocaleString()}P`);
            if (data.pointDeductedAsCash > 0)
                lines.push(`포인트 잔고 부족으로 ${data.pointDeductedAsCash.toLocaleString()}원 추가 공제`);

            Swal.fire({
                icon: 'success',
                title: '환불 완료',
                html: lines.join('<br/>'),
                confirmButtonColor: '#3085d6',
            });
        },
        onError: (error) => {
            Swal.fire({
                icon: 'error',
                title: '환불 실패',
                text: error.response?.data?.message || '환불 처리 중 오류가 발생했습니다.',
                confirmButtonColor: '#d33',
            });
        },
    });
};