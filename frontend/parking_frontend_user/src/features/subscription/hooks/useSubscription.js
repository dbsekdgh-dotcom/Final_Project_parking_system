import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
    getMySubscription,
    getSubscriptionHistory,
    getSubscriptionPolicy,
    readySubscription,
    confirmSubscription,
    refundSubscription,
} from '../api/subscriptionApi';
import Swal from 'sweetalert2';

// 1. 현재 활성 정기권 조회
export const useMySubscription = () => {
    return useQuery({
        queryKey: ['mySubscription'],
        queryFn: getMySubscription,
    });
};

// 2. 정기권 구매 이력 조회
export const useSubscriptionHistory = () => {
    return useQuery({
        queryKey: ['subscriptionHistory'],
        queryFn: getSubscriptionHistory,
    });
};

// 3. 정기권 정책 조회 (가격, 이용기간, 잔여 슬롯) - startDate 선택 시 호출
// startDate: "2025-05-01T00:00:00" 형식
export const useSubscriptionPolicy = (startDate) => {
    return useQuery({
        queryKey: ['subscriptionPolicy', startDate],
        queryFn: () => getSubscriptionPolicy(startDate),
        enabled: !!startDate,
        staleTime: 1000 * 60, // 1분간 캐시 유지
    });
};

// 5. 정기권 구매 준비 (orderId, amount, 기간 반환)
export const useReadySubscription = () => {
    return useMutation({
        mutationFn: readySubscription,
        onError: (error) => {
            Swal.fire({
                icon: 'error',
                title: '구매 준비 실패',
                text: error.response?.data?.message || '정기권 구매 준비 중 오류가 발생했습니다.',
                confirmButtonColor: '#d33',
            });
        },
    });
};

// 6. 정기권 결제 확정 (토스 승인 후 호출)
export const useConfirmSubscription = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: confirmSubscription,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['mySubscription'] });
            queryClient.invalidateQueries({ queryKey: ['subscriptionHistory'] });

            Swal.fire({
                icon: 'success',
                title: '정기권 발급 완료',
                text: '정기권이 정상적으로 발급되었습니다.',
                confirmButtonColor: '#3085d6',
            });
        },
        onError: (error) => {
            Swal.fire({
                icon: 'error',
                title: '결제 확정 실패',
                text: error.response?.data?.message || '결제 확정 중 오류가 발생했습니다.',
                confirmButtonColor: '#d33',
            });
        },
    });
};

// 7. 정기권 환불
export const useRefundSubscription = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: refundSubscription,
        onSuccess: (data) => {
            queryClient.invalidateQueries({ queryKey: ['mySubscription'] });
            queryClient.invalidateQueries({ queryKey: ['subscriptionHistory'] });

            Swal.fire({
                icon: 'success',
                title: '환불 완료',
                text: data?.message || '정기권이 취소되었습니다.',
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
