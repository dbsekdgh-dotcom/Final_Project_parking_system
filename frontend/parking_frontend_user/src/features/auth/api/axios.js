import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:8080', // 백엔드 서버 주소
    timeout: 5000, // 5초 타임아웃 설정
});

// [요청 인터셉터] 모든 요청에 AccessToken을 실어 보냅니다.
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem("accessToken");
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// [응답 인터셉터] 에러 발생 시 처리 로직
api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        // ⭐ 중요: 로그인(/login)이나 회원가입 과정에서 발생한 401/400 에러는 
        // 토큰 재발급 로직을 타지 않고 바로 에러를 반환해야 합니다.
        if (originalRequest.url.includes('/auth/local') || originalRequest.url.includes('/refresh')) {
            return Promise.reject(error);
        }

        // 401 Unauthorized 에러 발생 시 (토큰 만료 상황)
        if (error.response?.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true;

            try {
                const refreshToken = localStorage.getItem("refreshToken");
                
                if (!refreshToken) {
                    throw new Error("No refresh token found");
                }

                // 토큰 갱신 요청 (이때는 순수 axios 사용 권장)
                const res = await axios.post("http://localhost:8080/api/user/auth/refresh", {
                    refreshToken: refreshToken
                });

                if (res.status === 200) {
                    const newAccessToken = res.data.accessToken;
                    localStorage.setItem("accessToken", newAccessToken);

                    // 새 토큰으로 기존 요청 재시도
                    originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
                    return api(originalRequest);
                }
            } catch (refreshError) {
                // 리프레시 토큰도 만료된 경우
                console.error("세션이 만료되었습니다. 다시 로그인해주세요.");
                localStorage.clear();
                window.location.href = "/login";
                return Promise.reject(refreshError);
            }
        }

        
        return Promise.reject(error);
    }
);

export default api;