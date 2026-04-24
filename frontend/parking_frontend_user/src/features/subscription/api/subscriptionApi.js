import api from '../../auth/api/axios';

// 내 포인트 조회 (정기권 구매용)
export const getMyPointForSubscription = async () => {
    const response = await api.get('/api/user/subscriptions/point');
    return response.data;
};

// 정기권 정책 조회 (선택한 시작일 기준 잔여 수량 포함)
export const getSubscriptionPolicy = async (startDate = null) => {
    // Spring ISO_DATE_TIME은 'Z' 와 밀리초 미지원 → slice로 제거
    const params = startDate ? { startDate: startDate.toISOString().slice(0, 19) } : {};
    const response = await api.get('/api/user/subscriptions/policy', { params });
    return response.data;
};

// 내 정기권 목록 조회
export const getMySubscriptions = async () => {
    const response = await api.get('/api/user/subscriptions/my');
    return response.data;
};

// 정기권 구매
export const purchaseSubscription = async (subscriptionData) => {
    const response = await api.post('/api/user/subscriptions', subscriptionData);
    return response.data;
};

// 정기권 취소
export const cancelSubscription = async (subscriptionId) => {
    const response = await api.post(`/api/user/subscriptions/${subscriptionId}/cancel`);
    return response.data;
};
