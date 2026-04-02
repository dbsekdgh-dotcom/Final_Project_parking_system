import React from "react";
import "./SocialLoginButtons.css";

const SocialLoginButtons = () => {
    const handleLogin = (provider) => {
        const loginUrl = `http://localhost:8080/oauth2/authorization/${provider}`;
        window.location.href = loginUrl;
    };

    return (
        <div className="socialLoginButtons">
            <button
                className="socialLoginButton socialLoginButtonKakao"
                onClick={() => handleLogin("kakao")}
                aria-label="카카오 로그인"
                type="button"
            >
                카카오 로그인
            </button>
            <button
                className="socialLoginButton socialLoginButtonNaver"
                onClick={() => handleLogin("naver")}
                aria-label="네이버 로그인"
                type="button"
            >
                네이버 로그인
            </button>
        </div>
    );
};

export default SocialLoginButtons;