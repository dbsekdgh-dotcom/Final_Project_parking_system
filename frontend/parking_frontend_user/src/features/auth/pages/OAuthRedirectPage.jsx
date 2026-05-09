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
            console.log("[OAuth] 전체 파라미터:", Object.fromEntries(searchParams.entries()));
            const error = searchParams.get("error");
            if (error) console.log("[OAuth] 에러 코드:", error);

            // 한글 이름 깨짐 방지 및 데이터 추출
            const rawName = searchParams.get("name");
            const name = rawName ? decodeURIComponent(rawName) : null;
            const userId = searchParams.get("userId");
            const email = searchParams.get("email");
            const userStatus = searchParams.get("userStatus");
            const unitNo = searchParams.get("unitNo");

            // 1. 에러 처리
            if (error) {
                isprocessed.current = true;

                if (error === "email_mismatch") {
                    await Swal.fire({
                        icon: 'warning',
                        title: '계정 불일치',
                        text: '현재 로그인된 계정 정보와 일치하는 소셜 계정만 연동할 수 있습니다.',
                        confirmButtonText: '확인',
                        confirmButtonColor: '#3085d6',
                    });
                } else {
                    await Swal.fire({
                        icon: 'error',
                        title: '오류',
                        text: '소셜 로그인 중 오류가 발생했습니다.',
                        confirmButtonText: '확인',
                        confirmButtonColor: '#d33',
                    });
                }
                navigate("/", { replace: true });
                return;
            }

            // 2. 성공 처리 (토큰은 HttpOnly 쿠키로 자동 저장됨)
            if (name || email) {
                isprocessed.current = true;

                // UI용 사용자 정보만 localStorage에 저장 (토큰 제외)
                if (userId) {
                    localStorage.setItem("userId", userId);
                }
                if (name) {
                    localStorage.setItem("userName", name);
                }
                if (email) {
                    localStorage.setItem("userEmail", email);
                }
                if (userStatus) {
                    localStorage.setItem("userStatus", userStatus);
                }
                if (unitNo) {
                    localStorage.setItem("unitNo", unitNo);
                }

                // 환영 메시지용 세션 정보 저장
                sessionStorage.setItem("sessionActive", "true");
                sessionStorage.setItem("loginSuccess", name || "사용자");

                navigate("/dashboard", { replace: true });
            } else {
                // 정보가 없는 비정상적인 접근 처리
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
