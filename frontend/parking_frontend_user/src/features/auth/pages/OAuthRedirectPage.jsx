import React, { useEffect, useRef } from 'react' // useRef 추가
import { useNavigate, useSearchParams } from 'react-router-dom'

const OAuthRedirectPage = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    
    // ⭐ 중복 실행 방지를 위한 flag
    const isprocessed = useRef(false);

    useEffect(() => {
        // 이미 처리되었다면 실행하지 않음
        if (isprocessed.current) return;

        const error = searchParams.get("error");
        const accessToken = searchParams.get("accessToken");
        const refreshToken = searchParams.get("refreshToken");

        // 1. 에러 처리
        if (error) {
            isprocessed.current = true; // 처리 완료 표시
            
            if (error === "email_mismatch") {
                alert("현재 로그인된 계정 정보와 일치하는 소셜 계정만 연동할 수 있습니다.");
            } else {
                alert("소셜 로그인 중 오류가 발생했습니다.");
            }
            
            // 파라미터를 지우면서 대시보드로 이동
            navigate("/dashboard", { replace: true });
            return;
        }

        // 2. 성공 처리
        if (accessToken && refreshToken) {
            isprocessed.current = true;
            localStorage.setItem("accessToken", accessToken);
            localStorage.setItem("refreshToken", refreshToken);
            navigate("/dashboard", { replace: true });
        }
    }, [searchParams, navigate]);

    return (
        <div style={{ textAlign: 'center', marginTop: '100px' }}>
            <h2>인증 처리 중입니다...</h2>
        </div>
    )
}

export default OAuthRedirectPage;