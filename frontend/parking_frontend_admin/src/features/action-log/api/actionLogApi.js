import adminApi from "../../../shared/api/adminApi";

export const getActionLogs = async (params) =>{
    const response = await adminApi.get('/action-logs',{ params });
    return response.data;
}
export const revertActionLog = async (actionId) => {
    const response = await adminApi.post(`action-logs/${actionId}/revert`);
    return response.data;
}
export const getActivityLogs = async (params) =>{
    const response = await adminApi.get(`/activity-logs`,{ params });
    return response.data.data;
}
export const getActivityLog = async (id) =>{
    const response = await adminApi.get(`/activity-logs/${id}`);
    return response.data.data;
}