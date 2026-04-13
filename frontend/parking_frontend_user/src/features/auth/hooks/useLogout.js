import { useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import Swal from 'sweetalert2';
import api from '../api/axios'; // axios 인스턴스 가져오기

const useLogout = () => {
    const navigate = useNavigate();
    const queryClient = useQueryClient();

    const logout = async () => {
        try {
            // ⭐ 1. 서버에 로그아웃 요청 (Redis 리프레시 토큰 삭제)
            // 토큰이 만료되었을 수도 있으므로 에러가 나더라도 무시하고 진행하도록 try-catch 활용
            await api.post("/api/user/auth/local/logout");
        } catch (error) {
        }

        // 2. 로컬 스토리지 삭제
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        localStorage.removeItem("userName");
        localStorage.removeItem("userEmail");
        localStorage.removeItem("userStatus");
        localStorage.removeItem("unitNo");

        // 3. 임시 인증 쿠키 삭제
        document.cookie = "temp_jwt=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT";

        // 4. 리액트 쿼리 캐시 전체 초기화
        queryClient.clear();

        // 5. 알림 및 페이지 이동
        await Swal.fire({
            icon: 'success',
            title: '로그아웃',
            text: '로그아웃 되었습니다.',
            confirmButtonText: '확인',
            confirmButtonColor: '#3085d6',
        });
        
        navigate("/", { replace: true });
    };

    return { logout };
};

export default useLogout;