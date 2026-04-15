import axios from 'axios';

const BASE_URL = import.meta.env.VITE_API_BASE_URL;

const api = axios.create({
    baseURL: BASE_URL,
    timeout: 5000,
});

// 토큰 갱신 중 여부 플래그 & 대기 큐
let isRefreshing = false;
let failedQueue = [];

// 대기 중인 요청들을 일괄 처리
const processQueue = (error, token = null) => {
    failedQueue.forEach(({ resolve, reject }) => {
        if (error) reject(error);
        else resolve(token);
    });
    failedQueue = [];
};

// [요청 인터셉터] 모든 API 호출 시 헤더에 AccessToken 첨부
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem("accessToken");
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// [응답 인터셉터] 401 에러 발생 시 토큰 갱신 로직 실행
api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        // 인증이 필요 없는 공개 경로는 재발급 로직에서 제외
        const publicAuthEndpoints = [
            '/auth/local/login',
            '/auth/local/signup',
            '/auth/refresh',
        ];
        if (publicAuthEndpoints.some(ep => originalRequest.url.includes(ep))) {
            return Promise.reject(error);
        }

        if (error.response?.status === 401 && !originalRequest._retry) {

            // 이미 갱신 중이면 큐에 넣고 대기
            if (isRefreshing) {
                return new Promise((resolve, reject) => {
                    failedQueue.push({ resolve, reject });
                }).then(token => {
                    originalRequest.headers.Authorization = `Bearer ${token}`;
                    return api(originalRequest);
                }).catch(err => Promise.reject(err));
            }

            originalRequest._retry = true;
            isRefreshing = true;

            try {
                const refreshToken = localStorage.getItem("refreshToken");
                if (!refreshToken) throw new Error("No refresh token found");

                const res = await axios.post(`${BASE_URL}/api/user/auth/refresh`, {}, {
                    headers: { Authorization: `Bearer ${refreshToken}` }
                });

                const { accessToken, refreshToken: newRefreshToken } = res.data;

                localStorage.setItem("accessToken", accessToken);
                if (newRefreshToken) {
                    localStorage.setItem("refreshToken", newRefreshToken);
                }

                // 대기 중이던 요청들 새 토큰으로 일괄 재시도
                processQueue(null, accessToken);

                originalRequest.headers.Authorization = `Bearer ${accessToken}`;
                return api(originalRequest);

            } catch (refreshError) {
                processQueue(refreshError, null);
                localStorage.clear();
                window.location.href = "/";
                return Promise.reject(refreshError);
            } finally {
                isRefreshing = false;
            }
        }

        return Promise.reject(error);
    }
);

export default api;
