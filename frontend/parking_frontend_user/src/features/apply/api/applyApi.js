import api from '../../auth/api/axios';

export const fetchUnitStatus = async () => {
    try {
        const response = await api.get('/api/user/apply/unit-status');
        return response.data;
    } catch (error) {
        throw error;
    }
}


export const postResidentApply = async (unitNo) => {
    try {
        const response = await api.post('/api/user/apply/resident', { unitNo });
        return response.data
    } catch(error) {
        throw error;
    }
}