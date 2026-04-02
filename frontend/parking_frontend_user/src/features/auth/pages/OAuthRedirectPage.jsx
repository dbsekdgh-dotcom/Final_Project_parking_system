import React, { useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'

const OAuthRedirectPage = () => {

    const [searchParams] = useSearchParams();
    const navigate = useNavigate();

    useEffect(() => {
        const accessToken = searchParams.get("accessToken")
        const refreshToken = searchParams.get("refreshToken")

        if (accessToken && refreshToken) {
            localStorage.setItem("accessToken", accessToken)
            localStorage.setItem("refreshToken",refreshToken)

            console.log("로그인 성공! 토큰 저장됨")

            navigate("/dashboard", { replace: true })
        } else {
            console.error("토큰 추출 실패")
            navigate("/", { replace: true })
        }
    }, [searchParams, navigate])


    return (
        <div>
            <h2>로그인 인증 중입니다...</h2>
            <p>잠시만 기다려주세요.</p>
        </div>
    )
}

export default OAuthRedirectPage