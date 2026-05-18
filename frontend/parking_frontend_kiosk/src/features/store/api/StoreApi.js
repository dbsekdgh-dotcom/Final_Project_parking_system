import storeApi from "../../../shared/api/storeApi";

const host = '/api/store';

export const getStoreTestHint = async (storeId) => {
    const res = await storeApi.get(`${host}/test-hint`, { params: { storeId } });
    return res.data;
};


// 로그인 요청
export const storeLogin = async (password) => {
    const res = await storeApi.post(`${host}/login`,{ terminalPassword: password });
    return res.data;
};
// 상가 정보, 지갑 목록
export const getStoreMe = async () =>{
    const res = await storeApi.get(`${host}/me`);
    return res.data;
};

// 구매 가능한 ticket_policy 목록
export const getTicketPolicies = async () => {
    const res = await storeApi.get(`${host}/tickets/policies`);
    return res.data;
};
// 현재 보유 지갑 목록
export const getWallets = async () => {
    const res = await storeApi.get(`${host}/wallets`);
    return res.data;
};
// 구매 준비
export const purchaseReady = async (ticketPolicyId, quantity) =>{
    const res = await storeApi.get(`${host}/tickets/purchase/ready`,
        { params: { ticketPolicyId , quantity } });
    return res.data;
};
// 결제 완료 후 서버 확정
export const purchaseConfirm = async (ticketPolicyId, quantity) =>{
    const res = await storeApi.post(`${host}/tickets/purchase/confirm`, null,
        { params: { ticketPolicyId, quantity} });
    return res.data;
};
// 차량 검색
export const searchStoreCar = async (query) =>{
    const res = await storeApi.get(`${host}/search-car`,
      { params: { query } })
    return res.data;
};
// 할인권 적용
export const applyTicket = async (parkingLogId, ticketPolicyId, quantity) =>{
    const res = await storeApi.post(`${host}/tickets/apply`,
        { parkingLogId, ticketPolicyId, quantity });
    return res.data;
}