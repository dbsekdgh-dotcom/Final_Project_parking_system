import React, { useEffect, useRef } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import Swal from 'sweetalert2'

const OAuthRedirectPage = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    
    // ⭐ 중복 실행 방지를 위한 flag
    const isprocessed = useRef(false);

    useEffect(() => {
        if (isprocessed.current) return;

        const run = async () => {
            const error = searchParams.get("error");
            const accessToken = searchParams.get("accessToken");
            const refreshToken = searchParams.get("refreshToken");

            // 1. 에러 처리
            if (error) {
                isprocessed.current = true;

                if (error === "email_mismatch") {
                    await Swal.fire({
                        icon: 'warning',
                        title: '계정 불일치',
                        text: '현재 로그인된 계정 정보와 일치하는 소셜 계정만 연동할 수 있습니다.',
                        confirmButtonColor: '#3085d6',
                    });
                } else {
                    await Swal.fire({
                        icon: 'error',
                        title: '오류',
                        text: '소셜 로그인 중 오류가 발생했습니다.',
                        confirmButtonColor: '#d33',
                    });
                }

                navigate("/dashboard", { replace: true });
                return;
            }

            // 2. 성공 처리
            if (accessToken && refreshToken) {
                isprocessed.current = true;
                const name = searchParams.get("name");
                const email = searchParams.get("email");
                localStorage.setItem("accessToken", accessToken);
                localStorage.setItem("refreshToken", refreshToken);
                if (name) localStorage.setItem("userName", name);
                if (email) localStorage.setItem("userEmail", email);
                sessionStorage.setItem("loginSuccess", name || "사용자");
                navigate("/dashboard", { replace: true });
            }
        };

        run();
    }, [searchParams, navigate]);

    return (
        <div style={{ textAlign: 'center', marginTop: '100px' }}>
            <h2>인증 처리 중입니다...</h2>
        </div>
    )
}

export default OAuthRedirectPage;