import axios from "axios";

const host = (import.meta.env.VITE_API_BASE_URL || '') + '/api/store';

const authHeader = () =>{
    const token = localStorage.getItem('storeToken');
    return token ? { Authorization: `Bearer ${token}` } : {};
};
// 로그인 요청
export const storeLogin = async (password) => {
    const res = await axios.post(`${host}/login`,{ terminalPassword: password });
    return res.data;
};
// 상가 정보, 지갑 목록
export const getStoreMe = async () =>{
    const res = await axios.get(`${host}/me`, { headers: authHeader() });
    return res.data;
};

// 구매 가능한 ticket_policy 목록
export const getTicketPolicies = async () => {
    const res = await axios.get(`${host}/tickets/policies`, { headers: authHeader() });
    return res.data;
};
// 현재 보유 지갑 목록
export const getWallets = async () => {
    const res = await axios.get(`${host}/wallets`,{ headers: authHeader() });
    return res.data;
};
// 구매 준비
export const purchaseReady = async (ticketPolicyId, quantity) =>{
    const res = await axios.get(`${host}/tickets/purchase/ready`,
        { params: { ticketPolicyId , quantity }, headers: authHeader() });
    return res.data;
};
// 결제 완료 후 서버 확정
export const purchaseConfirm = async (ticketPolicyId, quantity) =>{
    const res = await axios.post(`${host}/tickets/purchase/confirm`, null,
        { params: { ticketPolicyId, quantity}, headers: authHeader() });
    return res.data;
};
// 차량 검색
export const searchStoreCar = async (query) =>{
    const res = await axios.get(`${host}/search-car`,
      { params: { query }, headers: authHeader() })
    return res.data;
};
// 할인권 적용
export const applyTicket = async (parkingLogId, ticketPolicyId, quantity) =>{
    const res = await axios.post(`${host}/tickets/apply`,
        { parkingLogId, ticketPolicyId, quantity }, { headers: authHeader() });
    return res.data;
}