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

export const activateStore = async (storeId) => {
    await adminApi.post(`/stores/${storeId}/activate`);
};

export const deactivateStore = async (storeId) => {
    await adminApi.post(`/stores/${storeId}/deactivate`)
};