import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:8080', // 기본 주소 설정
    timeout: 5000, // 5초 넘으면 타임아웃
});

api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem("accessToken")
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config

        if (error.response?.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true

            try {
                const refreshToken = localStorage.getItem("refreshToken")

                const res = await axios.post("http://localhost:8080/api/user/auth/refresh", {
                    refreshToken: refreshToken
                })

                if (res.status === 200) {
                    const newAccessToken = res.data.accessToken
                    localStorage.setItem("accessToken", newAccessToken)

                    originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
                    return api(originalRequest)
                }
            } catch (refreshError) {
                localStorage.clear()
                window.location.href = "/login"
                return Promise.reject(refreshError)
            }

        }
        return Promise.reject(error)

    }
)

export default api;