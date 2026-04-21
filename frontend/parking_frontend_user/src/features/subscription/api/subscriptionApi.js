import api from '../../auth/api/axios';

// 내 현재 활성 정기권 조회 (없으면 null)
export const getMySubscription = async () => {
    const response = await api.get('/api/user/subscriptions/my');
    return response.data;
};

// 정기권 구매 이력 조회
export const getSubscriptionHistory = async () => {
    const response = await api.get('/api/user/subscriptions/history');
    return response.data;
};

// 정기권 구매 준비 (슬롯·중복 검증 후 orderId 반환)
// body: { carNumber, startDate (LocalDateTime ISO), amount }
export const readySubscription = async (requestData) => {
    const response = await api.post('/api/user/subscriptions/ready', requestData);
    return response.data;
};

// 정기권 결제 확정 (토스 승인 결과 전달)
// body: { paymentKey, orderId, amount, carNumber, startDate, endDate }
export const confirmSubscription = async (confirmData) => {
    const response = await api.post('/api/user/subscriptions/confirm', confirmData);
    return response.data;
};

// 정기권 환불 (시작 전 전액 / 시작 후 일할 부분환불)
export const refundSubscription = async (subscriptionId) => {
    const response = await api.post(`/api/user/subscriptions/${subscriptionId}/refund`);
    return response.data;
};

// 정기권 정책 조회 (가격, 이용기간, 잔여 슬롯)
// startDate: "2025-05-01T00:00:00" 형식 (LocalDateTime ISO)
export const getSubscriptionPolicy = async (startDate) => {
    const response = await api.get('/api/user/subscriptions/policy', {
        params: { startDate },
    });
    return response.data;
};
