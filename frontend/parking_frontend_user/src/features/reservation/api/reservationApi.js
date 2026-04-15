// src/features/reservation/api/reservationApi.js
import api from '../../auth/api/axios';

// 내 예약 목록 조회
export const getMyReservations = async () => {
    // api.get을 쓰면 자동으로 헤더에 토큰이 붙습니다.
    const response = await api.get('/api/user/reservations');
    return response.data;
};

/**
 * [방문 예약 정책 및 특정 날짜의 잔여 현황 조회] - **추가됨**
 * GET /api/user/reservations/policy?targetDate=YYYY-MM-DD
 */
export const getReservationPolicy = async (targetDate) => {
    const response = await api.get('/api/user/reservations/policy', {
        params: { targetDate }
    });
    return response.data;
}

// 방문 예약 신청
export const applyReservation = async (reservationData) => {
    const response = await api.post('/api/user/reservations', reservationData);
    return response.data;
};

// 예약 수정
export const updateReservation = async (reservationId, reservationData) => {
    const response = await api.put(`/api/user/reservations/${reservationId}`, reservationData);
    return response.data
}


// 예약 취소
export const cancelReservation = async (reservationId) => {
    const response = await api.patch(`/api/user/reservations/${reservationId}/cancel`);
    return response.data;
};

