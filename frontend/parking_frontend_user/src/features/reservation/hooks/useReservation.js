import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getMyReservations, cancelReservation, applyReservation } from '../api/reservationApi'
import Swal from 'sweetalert2' // Swal 임포트

export const useMyReservation = () => {
    return useQuery({
        queryKey: ['myReservations'],
        queryFn: getMyReservations,
    });
};

export const useCancelReservation = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: cancelReservation,
        onSuccess: () => {
            // 캐시 갱신
            queryClient.invalidateQueries({ queryKey: ['myReservations'] });
            
            // 성공 알림
            Swal.fire({
                icon: 'success',
                title: '취소 완료',
                text: '예약이 성공적으로 취소되었습니다.',
                confirmButtonColor: '#3085d6',
            });
        },
        onError: (error) => {
            // 실패 알림
            Swal.fire({
                icon: 'error',
                title: '취소 실패',
                text: error.response?.data?.message || '취소 중 오류가 발생했습니다.',
                confirmButtonColor: '#d33',
            });
        }
    });
}


export const useCreateReservation = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: applyReservation,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['myReservations'] });

            Swal.fire({
                icon: 'success',
                title: '신청 완료',
                text: '방문 예약 신청이 정상적으로 접수되었습니다.',
                confirmButtonColor: '#3085d6',
            });
        },
        onError: (error) => {
            Swal.fire({
                icon: 'error',
                title: '신청 실패',
                text: error.response?.data?.message || '입력 정보를 다시 확인해주세요.',
                confirmButtonColor: '#d33',
            });
        }
    });
}