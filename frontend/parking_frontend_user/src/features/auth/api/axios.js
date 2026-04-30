import axios from 'axios';

const BASE_URL = import.meta.env.VITE_API_BASE_URL;

const api = axios.create({
    baseURL: BASE_URL,
    timeout: 5000,
    withCredentials: true, // HttpOnly 쿠키 자동 전송
});

// 토큰 갱신 중 여부 플래그 & 대기 큐
let isRefreshing = false;
let failedQueue = [];

// 대기 중인 요청들을 일괄 처리
const processQueue = (error) => {
    failedQueue.forEach(({ resolve, reject }) => {
        if (error) reject(error);
        else resolve();
    });
    failedQueue = [];
};

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
                }).then(() => {
                    return api(originalRequest); // 쿠키가 자동으로 전송됨
                }).catch(err => Promise.reject(err));
            }

            originalRequest._retry = true;
            isRefreshing = true;

            try {
                // refreshToken 쿠키가 자동으로 전송됨 (withCredentials: true)
                // 서버에서 새 accessToken/refreshToken 쿠키를 Set-Cookie로 내려줌
                await api.post(`/api/user/auth/refresh`);

                processQueue(null);

                // 원래 요청 재시도 (새 accessToken 쿠키 자동 전송)
                return api(originalRequest);

            } catch (refreshError) {
                processQueue(refreshError);
                // 리프레시 실패 시 UI 데이터 정리 후 로그인 페이지로
                localStorage.removeItem("userName");
                localStorage.removeItem("userEmail");
                localStorage.removeItem("userStatus");
                localStorage.removeItem("unitNo");
                localStorage.removeItem("userId");
                sessionStorage.removeItem("sessionActive");
                sessionStorage.removeItem("loginSuccess");
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
