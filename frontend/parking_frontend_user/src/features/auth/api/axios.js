import axios from 'axios';

const BASE_URL = import.meta.env.VITE_API_BASE_URL;

const api = axios.create({
    baseURL: BASE_URL,
    timeout: 5000,
});

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

        // 401 Unauthorized 발생 시 (토큰 만료)
        if (error.response?.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true;

            try {
                const refreshToken = localStorage.getItem("refreshToken");
                if (!refreshToken) throw new Error("No refresh token found");

                // ⭐ 백엔드 규격에 맞춰 Header에 Bearer 토큰으로 Refresh 요청
                const res = await axios.post(`${BASE_URL}/api/user/auth/refresh`, {}, {
                    headers: {
                        Authorization: `Bearer ${refreshToken}`
                    }
                });

                if (res.status === 200) {
                    const { accessToken, refreshToken: newRefreshToken } = res.data;

                    // ⭐ RTR: 새로운 Access와 Refresh 토큰을 모두 저장
                    localStorage.setItem("accessToken", accessToken);
                    if (newRefreshToken) {
                        localStorage.setItem("refreshToken", newRefreshToken);
                    }

                    // 새 토큰으로 실패했던 기존 요청 재시도
                    originalRequest.headers.Authorization = `Bearer ${accessToken}`;
                    return api(originalRequest);
                }
            } catch (refreshError) {
                localStorage.clear();
                window.location.href = "/"; // 메인/로그인 페이지로 이동
                return Promise.reject(refreshError);
            }
        }
        return Promise.reject(error);
    }
);

export default api;