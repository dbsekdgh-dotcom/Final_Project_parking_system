import React from "react";
import "./SocialLoginButtons.css";
import kakaoIcon from "../../../assets/images/kakao_login_icon.png";
import naverIcon from "../../../assets/images/naver_login_icon.png";

const SocialLoginButtons = () => {
    const handleLogin = (provider) => {
        const loginUrl = `http://localhost:8081/oauth2/authorization/${provider}`;
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
                <img className="socialLoginButton__icon" src={kakaoIcon} alt="" aria-hidden />
                카카오 로그인
            </button>
            <button
                className="socialLoginButton socialLoginButtonNaver"
                onClick={() => handleLogin("naver")}
                aria-label="네이버 로그인"
                type="button"
            >
                <img className="socialLoginButton__icon" src={naverIcon} alt="" aria-hidden />
                네이버 로그인
            </button>
        </div>
    );
};

export default SocialLoginButtons;