import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useMutation } from "@tanstack/react-query";
import Swal from "sweetalert2";

// 스타일 및 아이콘
import "./LoginPage.css";
import localIcon from "../../../assets/images/local_login_icon.png";

// 분리된 컴포넌트
import SocialLoginButtons from "../components/SocialLoginButtons";
import FindAccountButtons from "../components/FindAccountButtons";

// 분리된 API 서비스 및 핸들러
import { authService } from "../api/authService";
import { handleAuthError } from "../utils/authErrorHandler";

// ✅ 통합된 유틸리티에서 모든 계정 관리 로직 가져오기
import { 
    handleAccountRecover, 
    openFindPwModal, 
    openEmailVerificationModal // 인증 전용 모달 추가
} from "../utils/accountUtils";

const LoginPage = () => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({ email: "", password: "" });
    const { email, password } = formData;

    const loginMutation = useMutation({
        // 1. 분리한 API 서비스 사용
        mutationFn: (loginData) => authService.login(loginData),
        
        onSuccess: (data) => {
            console.log("로그인 성공! 서버 응답:", data);
            
            // 토큰 및 사용자 정보 저장
            localStorage.setItem("accessToken", data.accessToken);
            localStorage.setItem("refreshToken", data.refreshToken);
            localStorage.setItem("userName", data.name);
            localStorage.setItem("userEmail", data.email);
            sessionStorage.setItem("loginSuccess", data.name);

            navigate("/dashboard");
        },
        
        onError: (error) => {
            // 2. 통합 에러 핸들러 사용
            handleAuthError(error, navigate, formData.email, {
                onVerify: (targetEmail) => {
                    // ✅ 미인증 또는 계정 잠금 시: 인증 전용 모달 연결
                    openEmailVerificationModal(targetEmail);
                },
                onRecover: (targetEmail) => {
                    // ✅ 탈퇴 계정 복구 로직 연결
                    handleAccountRecover({ email: targetEmail });
                }
            });
        }
    });

    const onChange = (e) => {
        setFormData({ ...formData, [e.target.id]: e.target.value });
    };

    const onLogin = (e) => {
        e.preventDefault();

        // 기본 유효성 검사
        if (!email || !password) {
            Swal.fire({ icon: 'warning', title: '입력 오류', text: '이메일과 비밀번호를 모두 입력해주세요.' });
            return;
        }

        if (password.length < 8) {
            Swal.fire({ icon: 'error', title: '비밀번호 형식 오류', text: '비밀번호는 최소 8자 이상이어야 합니다.' });
            return;
        }

        loginMutation.mutate(formData);
    };

    return (
        <div className="loginPage">
            <div className="loginCard">
                <header className="loginHeader">
                    <h1 className="loginLogo">Parking</h1>
                </header>

                <main className="loginMain">
                    <h2 className="loginTitle">로그인</h2>
                    <p className="loginSubtitle">계정에 로그인하여 서비스를 이용하세요.</p>

                    {/* 소셜 로그인 버튼 영역 */}
                    <SocialLoginButtons />

                    <div className="dividerRow">
                        <span className="dividerLine" />
                        <span className="dividerText">또는</span>
                        <span className="dividerLine" />
                    </div>

                    <form className="loginForm" onSubmit={onLogin}>
                        <label className="fieldLabel" htmlFor="email">이메일</label>
                        <input
                            id="email"
                            type="email"
                            placeholder="park@email.com"
                            className="fieldInput"
                            value={email}
                            onChange={onChange}
                            required
                        />

                        <label className="fieldLabel" htmlFor="password">비밀번호</label>
                        <input
                            id="password"
                            type="password"
                            placeholder="비밀번호"
                            className="fieldInput"
                            value={password}
                            onChange={onChange}
                            required
                        />

                        <button
                            type="submit"
                            className="loginButton"
                            disabled={loginMutation.isPending}
                        >
                            {!loginMutation.isPending && (
                                <img 
                                    src={localIcon} 
                                    alt="" 
                                    aria-hidden 
                                    style={{ width: 20, height: 20, verticalAlign: "middle", marginRight: 6 }} 
                                />
                            )}
                            {loginMutation.isPending ? "로그인 중..." : "로그인"}
                        </button>
                    </form>
                    
                    {/* 계정 찾기 버튼 영역 (현재 입력된 이메일 전달) */}
                    <FindAccountButtons currentEmail={email} />

                    <div className="bottomRow">
                        <span className="bottomText">계정이 없으신가요?</span>
                        <button
                            type="button"
                            className="signupButton"
                            onClick={() => navigate("/signup")}
                        >
                            회원가입
                        </button>
                    </div>
                </main>
            </div>
        </div>
    );
};

export default LoginPage;