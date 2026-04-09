import api from "../api/axios";

export const authService = {

    login: async (loginData) => {
        const response = await api.post("/api/user/auth/local/login", loginData);
        return response.data
    },
}