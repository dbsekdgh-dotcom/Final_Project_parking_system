import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getMyReservations, cancelReservation, applyReservation, getReservationPolicy, updateReservation } from '../api/reservationApi'
import Swal from 'sweetalert2' // Swal 임포트

/**
 * 1. 내 방문 예약 목록 조회 훅
 * 로그인한 사용자가 신청한 모든 예약 리스트를 가져옵니다.
 */
export const useMyReservation = () => {
    return useQuery({
        queryKey: ['myReservations'],
        queryFn: getMyReservations,
    });
};

/**
 * 2. 특정 날짜의 예약 정책 및 잔여 현황 조회 훅
 * 날짜 선택 시 해당 날짜의 단지 전체 잔여량과 본인의 이용 가능 횟수를 확인합니다.
 */
export const useReservationPolicy = (targetDate) => {
    return useQuery({
        queryKey: ['reservationPolicy', targetDate], // 날짜별로 데이터를 관리/캐싱
        queryFn: () => getReservationPolicy(targetDate),
        enabled: !!targetDate, // 날짜 값이 존재할 때만 API 호출 실행
        staleTime: 1000 * 60, // 1분간은 신선한 데이터로 간주하여 중복 호출 방지
    });
};

/**
 * 3. 방문 예약 취소 훅
 * 기존 예약을 취소 상태로 변경하고 목록을 새로고침합니다.
 */
export const useCancelReservation = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: cancelReservation,
        onSuccess: () => {
            // 취소 성공 시 목록 + 정책 데이터 갱신
            queryClient.invalidateQueries({ queryKey: ['myReservations'] });
            queryClient.invalidateQueries({ queryKey: ['reservationPolicy'] });
            
            // 성공 알림 창
            Swal.fire({
                icon: 'success',
                title: '취소 완료',
                text: '예약이 성공적으로 취소되었습니다.',
                confirmButtonColor: '#3085d6',
            });
        },
        onError: (error) => {
            // 실패 시 백엔드 에러 메시지(ErrorCode 기반) 출력
            Swal.fire({
                icon: 'error',
                title: '취소 실패',
                text: error.response?.data?.message || '취소 중 오류가 발생했습니다.',
                confirmButtonColor: '#d33',
            });
        }
    });
};

/**
 * 4. 방문 예약 신청 훅
 * 새로운 예약을 생성하고 성공 시 알림 및 캐시를 갱신합니다.
 */
export const useCreateReservation = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: applyReservation,
        onSuccess: () => {
            // 신청 성공 시 예약 목록과 정책 정보(잔여 횟수 등)를 모두 갱신
            queryClient.invalidateQueries({ queryKey: ['myReservations'] });
            queryClient.invalidateQueries({ queryKey: ['reservationPolicy'] });

            Swal.fire({
                icon: 'success',
                title: '신청 완료',
                text: '방문 예약 신청이 정상적으로 접수되었습니다.',
                confirmButtonColor: '#3085d6',
            });
        },
        onError: (error) => {
            // 실패 시 백엔드 Validation 에러 메시지 출력
            Swal.fire({
                icon: 'error',
                title: '신청 실패',
                text: error.response?.data?.message || '입력 정보를 다시 확인해주세요.',
                confirmButtonColor: '#d33',
            });
        }
    });
};

/**
 * 5. 방문 예약 수정 훅
 * 기존 예약 내용을 변경하고 성공 시 캐시를 최신화합니다.
 */
export const useUdateReservation = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: ({ reservationId, reservationDate }) =>
            updateReservation(reservationId, reservationDate),

        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['myReservations'] });
            queryClient.invalidateQueries({ queryKey: ['reservationPolicy'] });

            Swal.fire({
                icon: 'success',
                title: '수정 완료',
                text: '예약 정보가 성공적으로 변경되었습니다.',
                confirmButtonColor: '#3085d6',
            });
        },
        onError: (error) => {
            Swal.fire({
                icon: 'error',
                titile: '수정 실패',
                text: error.response?.data?.message || '예약 수정 중 오류가 발생했습니다.',
                confirmButtonColor: '#d33',
            });
        }
    });
};