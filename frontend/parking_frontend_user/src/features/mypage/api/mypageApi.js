import api from '../../auth/api/axios';

// JWT 토큰에서 userId 추출 (Principal) → URL에 userId 불필요
export const fetchMyInfo = () =>
    api.get('/api/user/mypage').then(r => r.data);

// phone 또는 birth 중 변경할 필드만 담아서 호출
export const updateMyInfo = (data) =>
    api.put('/api/user/mypage', data).then(r => r.data);