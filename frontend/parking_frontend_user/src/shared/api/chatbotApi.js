import axios from 'axios';

const AI_BASE_URL = import.meta.env.VITE_AI_SERVER_URL || 'http://localhost:8000';

const aiApi = axios.create({
    baseURL: AI_BASE_URL,
    withCredentials: true,
    timeout: 30000,
});

export const sendChatMessage = async (message, history = []) => {
    const response = await aiApi.post('/api/v1/parking/chatbot/ask', { message, history });
    return response.data;
};
