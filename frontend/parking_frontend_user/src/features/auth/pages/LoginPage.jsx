import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import SocialLoginButtons from "../components/SocialLoginButtons";
import "./LoginPage.css";
import { useMutation } from "@tanstack/react-query";
import axios from "../api/axios";

const LoginPage = () => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({ email: "", password: "" });
    const { email, password } = formData;

    const loginMutation = useMutation({
        mutationFn: async (loginData) => {
            // 백엔드의 UserLoginController 경로와 일치하는지 확인하세요!
            const response = await axios.post("/api/user/auth/local/login", loginData);
            return response.data;
        },
        onSuccess: (data) => {
            console.log("로그인 성공! 서버 응답:", data);
            
            // 백엔드 UserLoginResponseDto 구조에 맞춰 저장
            localStorage.setItem("accessToken", data.accessToken);
            localStorage.setItem("refreshToken", data.refreshToken);
            localStorage.setItem("userName", data.name);
            localStorage.setItem("userEmail", data.email);

            alert(`${data.name}님, 환영합니다!`);
            navigate("/dashboard"); // 성공 시 대시보드로 이동
        },
        onError: (error) => {
            // ⭐ 백엔드 UserAuthExceptionHandler에서 던진 에러 메시지 추출
            // error.response.data 구조: { status: 401, code: "LOGIN_FAILED", message: "..." }
            const serverMessage = error.response?.data?.message || "로그인 중 오류가 발생했습니다.";
            const errorCode = error.response?.data?.code;

            console.error(`로그인 실패 [${errorCode}]:`, serverMessage);
            alert(serverMessage); // 사용자에게 "비밀번호가 일치하지 않습니다" 등을 보여줌
        }
    });

    const onChange = (e) => {
        setFormData({ ...formData, [e.target.id]: e.target.value });
    };

    const onLogin = (e) => {
        e.preventDefault();
        
        // 간단한 프론트엔드 자체 검증
        if (!email || !password) {
            alert("이메일과 비밀번호를 모두 입력해주세요.");
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
                            {loginMutation.isPending ? "로그인 중..." : "로그인"}
                        </button>
                    </form>

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