import axios from 'axios';

const BASE_URL = import.meta.env.VITE_API_BASE_URL;

//1. Axios 인스턴스 생성
const adminApi = axios.create({
    baseURL: `${BASE_URL}/admin`, // 백엔드 관리자 공통 경로
    timeout: 10000, // 10초
});

//2. Request Intercepter: 모든 요청에 전에 실행(통행증 부착)
adminApi.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('accessToken');
        if(token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config;
    },
    (error) => Promise.reject(error)
);

//3. Response Intercepter: 응답을 받은 후 실행(만료 감시 및 자동 갱신)
adminApi.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        // 에러 코드가 401이고, 아직 재시도하지 않은 요청이라면
        if(error.response?.status === 401 && !originalRequest._retry){
            originalRequest._retry = true; // 무한루프 방지용 플래그
            console.log("Access Token 만료 감지, 재발급 시도 중..");

            try{
                const oldAccessToken = localStorage.getItem('accessToken');
                const refreshToken = localStorage.getItem('refreshToken');

                // 백엔드의  /admin/refresh 호출 (재발급 API)
                // 주의: 인스턴스(adminApi)가 아닌 생 axios를 써야 재귀 호출을 피함.
                const res = await axios.post(`${BASE_URL}/admin/refresh`, null, {
                    params: {refreshToken},
                    headers: {Authorization: `Bearer ${oldAccessToken}`}
                });

                //새 토큰들 저장
                const {accessToken, refreshToken: newRefreshToken} = res.data;
                localStorage.setItem('accessToken',accessToken);
                localStorage.setItem('refreshToken',newRefreshToken || refreshToken);

                //원래 요청에 새 토큰 갈아 끼우고 다시 쏘기
                originalRequest.headers.Authorization = `Bearer ${accessToken}`;
                return axios(originalRequest);
            }catch(refreshError){
                //리프레시 토큰까지 죽었다면 강제 로그아웃
                localStorage.clear();
                window.location.href = '/login'
                return Promise.reject(refreshError);
            }
        }
        return Promise.reject(error);
    }
);
export default adminApi;