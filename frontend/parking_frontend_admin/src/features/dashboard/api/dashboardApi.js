import adminApi from '../../../shared/api/adminApi'

// 통계카드 4개
export const getSummary = () =>
    adminApi.get('/dashboard/summary').then(r => r.data.data);
// 매출 현황 그래프
// type : TOTAL, TICKET, PARKING, SUBSCRIPTION
// 응답 :  totalAmount, changePercent, monthly: [{ month, amount}]
export const getRevenue = (type = 'TOTAL') =>
    adminApi.get('/dashboard/revenue', { params: { type } }).then(r => r.data);

//사용량 그래프
export const getUsage = (type = 'PARKING') =>
    adminApi.get('/dashboard/usage',{ params: { type } }).then(r => r.data.data);

// 매출 상세 현황 테이블
// 응답 { content: [{ data, category, amount, transactionCount }], totalPages }
export const getRevenueDetail = (type = 'TOTAL', page = 0, size = 5) =>
    adminApi.get('/dashboard/revenue/detail',{ params: { type, page, size } }).then(r => r.data);

// 사용량 상세 현황 테이블
export const getUsageDetail = (type = 'PARKING', page = 0, size = 5) =>
    adminApi.get('/dashboard/usage/detail',{ params: { type, page, size } }).then(r => r.data.data);
