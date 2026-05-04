import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081';

const aiApi = axios.create({
    baseURL: API_BASE_URL,
    withCredentials: true,
    timeout: 30000,
});

export const sendChatMessage = async (message, history = []) => {
    const response = await aiApi.post('/api/user/chatbot/ask', { message, history });
    return response.data;
};
