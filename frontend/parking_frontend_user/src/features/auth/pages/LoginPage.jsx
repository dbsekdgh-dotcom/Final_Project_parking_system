import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import SocialLoginButtons from "../components/SocialLoginButtons";
import "./LoginPage.css";
import { useMutation } from "@tanstack/react-query";
import axios from "../api/axios";
import Swal from "sweetalert2";
import FindAccountButtons from "../components/FindAccountButtons";
import localIcon from "../../../assets/images/local_login_icon.png";
// ⭐ 계정 복구 로직 함수 임포트
import { handleAccountRecover } from "../components/AccountRecoverButton"; 

const LoginPage = () => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({ email: "", password: "" });
    const { email, password } = formData;

    const loginMutation = useMutation({
        mutationFn: async (loginData) => {
            const response = await axios.post("/api/user/auth/local/login", loginData);
            return response.data;
        },
        onSuccess: (data) => {
            console.log("로그인 성공! 서버 응답:", data);

            localStorage.setItem("accessToken", data.accessToken);
            localStorage.setItem("refreshToken", data.refreshToken);
            localStorage.setItem("userName", data.name);
            localStorage.setItem("userEmail", data.email);
            sessionStorage.setItem("loginSuccess", data.name);

            navigate("/dashboard");
        },
        onError: (error) => {
            const serverMessage = error.response?.data?.message || "로그인 중 오류가 발생했습니다.";
            const errorCode = error.response?.data?.code;

            console.error(`로그인 실패 [${errorCode}]:`, serverMessage);

            // 1. 소셜 로그인 사용자 체크
            if (errorCode === "SOCIAL_USER_LOGIN_ATTEMPT") {
                Swal.fire({
                    icon: 'warning',
                    title: '이미 가입된 계정입니다',
                    text: '이 계정은 소셜 로그인으로 가입되었습니다. 상단의 버튼을 이용해 주세요!',
                    confirmButtonColor: '#3085d6',
                    confirmButtonText: '확인'
                });
            } 
            // 2. ⭐ 탈퇴한 계정인 경우 (복구 유도)
            else if (errorCode === "USER_WITHDRAWN" || serverMessage.includes("탈퇴")) {
                Swal.fire({
                    icon: 'error',
                    title: '탈퇴된 계정입니다',
                    html: `
                        <div style="margin: 15px 0;">
                            <p>${serverMessage}</p>
                            <p style="font-size: 14px; color: #666;">계정을 다시 사용하시려면 복구가 필요합니다.</p>
                        </div>
                        <div style="display: flex; justify-content: center; gap: 10px;">
                            <button id="swal-close-btn" class="swal2-confirm swal2-styled" style="background-color: #757575; width: 100px; cursor: pointer;">확인</button>
                            <button id="swal-recover-btn" class="swal2-confirm swal2-styled" style="background-color: #f8bb86; color: #000; font-weight: bold; width: 120px; cursor: pointer;">계정 복구</button>
                        </div>
                    `,
                    showConfirmButton: false // 커스텀 버튼을 위해 기본 버튼 숨김
                });

                // 커스텀 버튼 이벤트 바인딩
                setTimeout(() => {
                    const closeBtn = document.getElementById('swal-close-btn');
                    const recoverBtn = document.getElementById('swal-recover-btn');

                    if (closeBtn) closeBtn.onclick = () => Swal.close();
                    if (recoverBtn) {
                        recoverBtn.onclick = () => {
                            Swal.close();
                            // 복구 로직 실행 (입력했던 이메일을 초깃값으로 전달)
                            handleAccountRecover({ email: formData.email });
                        };
                    }
                }, 100);
            }
            // 3. 존재하지 않는 계정 (회원가입 유도)
            else if (errorCode === "USER_NOT_FOUND") {
                Swal.fire({
                    icon: 'question',
                    title: '계정을 찾을 수 없습니다',
                    text: '가입되지 않은 이메일입니다. 회원가입 페이지로 이동할까요?',
                    showCancelButton: true,
                    confirmButtonColor: '#3085d6',
                    cancelButtonColor: '#6c757d',
                    confirmButtonText: '이동하기',
                    cancelButtonText: '취소'
                }).then((result) => {
                    if (result.isConfirmed) {
                        navigate("/signup");
                    }
                });
            } 
            // 4. 기타 에러 (비밀번호 불일치 등)
            else {
                Swal.fire({
                    icon: 'error',
                    title: '로그인 실패',
                    text: serverMessage,
                    confirmButtonColor: '#3085d6',
                });
            }
        }
    });

    const onChange = (e) => {
        setFormData({ ...formData, [e.target.id]: e.target.value });
    };

    const onLogin = (e) => {
        e.preventDefault();

        if (!email || !password) {
            Swal.fire({
                icon: 'warning',
                title: '입력 오류',
                text: '이메일과 비밀번호를 모두 입력해주세요.',
                confirmButtonColor: '#3085d6',
            });
            return;
        }

        if (password.length < 8) {
            Swal.fire({
                icon: 'error',
                title: '비밀번호 형식 오류',
                text: '비밀번호는 최소 8자 이상이어야 합니다. 다시 확인해 주세요.',
                confirmButtonColor: '#d33',
            })
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
                            {!loginMutation.isPending && (
                                <img src={localIcon} alt="" aria-hidden style={{ width: 20, height: 20, verticalAlign: "middle", marginRight: 6 }} />
                            )}
                            {loginMutation.isPending ? "로그인 중..." : "로그인"}
                        </button>
                    </form>
                    
                    {/* 아이디/비밀번호 찾기 버튼 컴포넌트 */}
                    <FindAccountButtons />

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