import axios from "axios";

const storeApi = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '',
    timeout: 10000,
});

storeApi.interceptors.request.use((config) => {
    const token = localStorage.getItem('storeToken');
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
});

export default storeApi