import { useQueryClient } from '@tanstack/react-query';
// import React from 'react'
import { useNavigate } from 'react-router-dom';
import Swal from 'sweetalert2';


const useLogout = () => {
    const navigate = useNavigate();
    const queryClient = useQueryClient();

    const logout = async () => {
        // 1. 로컬 스토리지 삭제
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        localStorage.removeItem("userName");
        localStorage.removeItem("userEmail");

        // 2. 임시 인증 쿠키 삭제 (1970년 설정으로 브라우저가 즉시 파기)
        document.cookie = "temp_jwt=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT";

        // 3. 리액트 쿼리 캐시 전체 초기화 (이전 사용자 데이터 잔상 방지)
        queryClient.clear();

        // 4. 로그인/메인 화면으로 이동 (replace: true로 뒤로가기 방지)
        await Swal.fire({
            icon: 'success',
            title: '로그아웃',
            text: '로그아웃 되었습니다.',
            timer: 1500,
            showConfirmButton: false,
        });
        navigate("/", { replace: true });
    };

    return { logout };
};

export default useLogout;