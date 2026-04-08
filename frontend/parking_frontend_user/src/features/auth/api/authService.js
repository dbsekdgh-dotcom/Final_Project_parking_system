import api from "../api/axios";

export const authService = {

    login: async (loginData) => {
        const response = await api.post("/api/user/auth/local/login", loginData);
        return response.data
    },

    checkEmail: async (email) => {
        const response = await api.get(`/api/user/auth/check-email?email=${email}`);
        return response.data;
    }
}