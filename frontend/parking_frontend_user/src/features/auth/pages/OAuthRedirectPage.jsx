import React, { useEffect, useRef } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import Swal from 'sweetalert2'

const OAuthRedirectPage = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    
    // ⭐ 중복 실행 방지를 위한 flag (React StrictMode 대응)
    const isprocessed = useRef(false);

    useEffect(() => {
        // 이미 처리가 시작되었다면 중복 실행 안 함
        if (isprocessed.current) return;

        const run = async () => {
            const error = searchParams.get("error");
            const accessToken = searchParams.get("accessToken");
            const refreshToken = searchParams.get("refreshToken");
            
            // 한글 이름 깨짐 방지 및 데이터 추출
            const rawName = searchParams.get("name");
            const name = rawName ? decodeURIComponent(rawName) : null;
            const email = searchParams.get("email");


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
                navigate("/", { replace: true });
                return;
            }

            // 2. 성공 처리
            if (accessToken && refreshToken) {
                isprocessed.current = true;

                // 로컬 스토리지 저장 (프로젝트 공통 키 이름인 userName, userEmail 사용)
                localStorage.setItem("accessToken", accessToken);
                localStorage.setItem("refreshToken", refreshToken);
                
                if (name) {
                    localStorage.setItem("userName", name);
                }
                if (email) {
                    localStorage.setItem("userEmail", email);
                }

                // 환영 메시지용 세션 정보 저장
                sessionStorage.setItem("loginSuccess", name || "사용자");

                // 저장 완료 후 대시보드로 이동
                // (데이터 반영을 확실히 하기 위해 가끔 window.location.href="/dashboard"를 쓰기도 합니다)
                navigate("/dashboard", { replace: true });
            } else {
                // 토큰이 없는 비정상적인 접근 처리
                navigate("/", { replace: true });
            }
        };

        run();
    }, [searchParams, navigate]);

    return (
        <div style={{ 
            textAlign: 'center', 
            marginTop: '100px',
            fontFamily: 'Arial, sans-serif'
        }}>
            <div className="spinner" style={{
                border: '4px solid #f3f3f3',
                borderTop: '4px solid #3498db',
                borderRadius: '50%',
                width: '40px',
                height: '40px',
                animation: 'spin 1s linear infinite',
                margin: '0 auto 20px'
            }}></div>
            <style>{`
                @keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
            `}</style>
            <h2>로그인 인증 처리 중입니다...</h2>
            <p>잠시만 기다려 주세요.</p>
        </div>
    )
}

export default OAuthRedirectPage;