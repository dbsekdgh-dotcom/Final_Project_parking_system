import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081';

const aiApi = axios.create({
    baseURL: API_BASE_URL,
    withCredentials: true,
    timeout: 30000,
});

let isRefreshing = false;

aiApi.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;
        if (error.response?.status === 401 && !originalRequest._retry) {
            if (isRefreshing) return Promise.reject(error);
            originalRequest._retry = true;
            isRefreshing = true;
            try {
                await axios.post(`${API_BASE_URL}/api/user/auth/refresh`, {}, { withCredentials: true });
                return aiApi(originalRequest);
            } catch {
                window.location.href = '/';
                return Promise.reject(error);
            } finally {
                isRefreshing = false;
            }
        }
        return Promise.reject(error);
    }
);

export const sendChatMessage = async (message, history = []) => {
    const response = await aiApi.post('/api/user/chatbot/ask', { message, history });
    return response.data;
};
