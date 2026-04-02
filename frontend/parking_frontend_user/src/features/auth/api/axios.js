import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:8080', // 기본 주소 설정
    timeout: 5000, // 5초 넘으면 타임아웃
});

export default api;