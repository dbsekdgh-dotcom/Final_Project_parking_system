// src/features/reservation/api/reservationApi.js
import api from '../../auth/api/axios';

// 내 예약 목록 조회
export const getMyReservations = async () => {
    // api.get을 쓰면 자동으로 헤더에 토큰이 붙습니다.
    const response = await api.get('/api/user/reservations');
    return response.data;
};

// 방문 예약 신청
export const applyReservation = async (reservationData) => {
    const response = await api.post('/api/user/reservations', reservationData);
    return response.data;
};

// 예약 취소
export const cancelReservation = async (reservationId) => {
    const response = await api.patch(`/api/user/reservations/${reservationId}/cancel`);
    return response.data;
};