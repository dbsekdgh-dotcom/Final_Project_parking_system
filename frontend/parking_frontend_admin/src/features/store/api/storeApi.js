import adminApi from '../../../shared/api/adminApi';

export const getStores = async (params) =>{
    const res = await adminApi.get('/stores',{ params });
    return res.data;
};

export const getStore = async (storeId) =>{
    const res = await adminApi.get(`/stores/${storeId}`);
    return res.data;
};

export const updateStore = async (storeId, dto) =>{
    await adminApi.patch(`/stores/${storeId}`,dto)
};

export const activateStore = async (storeId, dto) => {
    await adminApi.post(`/stores/${storeId}/activate`, dto);
};

export const deactivateStore = async (storeId) => {
    await adminApi.post(`/stores/${storeId}/deactivate`)
};

export const getStoreTicketConfig = async (storeId) =>{
    const res = await adminApi.get(`/stores/${storeId}/ticket-config`);
    return res.status === 204 ? null : res.data;    
};

export const setStoreTicketConfig = async (storeId, dto)=>{
    await adminApi.put(`/stores/${storeId}/ticket-config`,dto);
};
export const getFreeTicketPolicies = async () => {
    const res = await adminApi.get('/stores/free-ticket-policies');
    return res.data;
};
