import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import SocialLoginButtons from "../components/SocialLoginButtons";
import "./LoginPage.css";
import { useMutation } from "@tanstack/react-query";
import axios from "../api/axios";

const LoginPage = () => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({ email: "", password: ""});
    const { email, password} = formData;

    const loginMutation = useMutation({
        mutationFn: async (loginData) => {
            const response = await axios.post("/api/user/auth/local/login", loginData)
            return response.data;
        },
        onSuccess: (data) => {
            console.log("서버가 준 데이터 전체:", data);
            localStorage.setItem("accessToken", data.accessToken);
            localStorage.setItem("refreshToken", data.refreshToken);
            localStorage.setItem("userName", data.name);

            alert(`${data.name}님, 환영합니다!`);
            navigate("/dashboard")
        },
        onError: (error) => {
            const serverMessage = error.response?.data?.message || "로그인 정보가 올바르지 않습니다."
            alert(serverMessage);
        }
    });
    const onChange = (e) => {
        setFormData({...formData, [e.target.id]: e.target.value});
    };

    const onLogin = (e) => {
        e.preventDefault();
        loginMutation.mutate(formData)
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
                            value={email}      // ⭐ 상태 연결
                            onChange={onChange} // ⭐ 핸들러 연결
                            required 
                        />

                        <label className="fieldLabel" htmlFor="password">비밀번호</label>
                        <input 
                            id="password" 
                            type="password" 
                            placeholder="비밀번호" 
                            className="fieldInput"
                            value={password}   // ⭐ 상태 연결
                            onChange={onChange} // ⭐ 핸들러 연결
                            required 
                        />

                        {/* ⭐ type="submit"으로 변경 및 로딩 중 비활성화 */}
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