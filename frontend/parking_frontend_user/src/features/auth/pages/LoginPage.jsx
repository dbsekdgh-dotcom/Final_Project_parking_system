import React from "react";
import { useNavigate } from "react-router-dom";
import SocialLoginButtons from "../components/SocialLoginButtons";
import "./LoginPage.css";

const LoginPage = () => {
    const navigate = useNavigate();

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

                    <form className="loginForm">
                        <label className="fieldLabel" htmlFor="email">이메일</label>
                        <input id="email" type="email" placeholder="park@email.com" className="fieldInput" />

                        <label className="fieldLabel" htmlFor="password">비밀번호</label>
                        <input id="password" type="password" placeholder="비밀번호" className="fieldInput" />

                        <button type="button" className="loginButton">로그인</button>
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