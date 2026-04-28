import React, { useEffect, useRef } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { handleSocialRecover } from "../utils/accountUtils.js";
import "./SocialLoginButtons.css";
import kakaoIcon from "../../../assets/images/kakao_login_icon.png";
import naverIcon from "../../../assets/images/naver_login_icon.png";

const SocialLoginButtons = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const isProcessing = useRef(false);

    useEffect(() => {
        const params = new URLSearchParams(location.search);
        const error = params.get("error");
        const email = params.get("email");

        if (error === "WITHDRAWN" && email && !isProcessing.current) {
            isProcessing.current = true;
            
            const recoveryData = {
                email,
                provider: params.get("provider"),
                providerId: params.get("providerId"),
            };
            
            // 유틸리티 함수 호출
            handleSocialRecover(recoveryData, () => {
                isProcessing.current = false;
                navigate("/", { replace: true });
            });
        }
    }, [location, navigate]);

    const handleLogin = (provider) => {
        window.location.href = `/oauth2/authorization/${provider}`;
    };

    return (
        <div className="socialLoginButtons">
            <button className="socialLoginButton socialLoginButtonKakao" onClick={() => handleLogin("kakao")}>
                <img className="socialLoginButton__icon" src={kakaoIcon} alt="" /> 카카오 로그인
            </button>
            <button className="socialLoginButton socialLoginButtonNaver" onClick={() => handleLogin("naver")}>
                <img className="socialLoginButton__icon" src={naverIcon} alt="" /> 네이버 로그인
            </button>
        </div>
    );
};

export default SocialLoginButtons;