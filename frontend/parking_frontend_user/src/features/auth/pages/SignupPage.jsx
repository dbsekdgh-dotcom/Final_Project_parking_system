import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./SignupPage.css";
import { useMutation } from "@tanstack/react-query";
import api from "../api/axios";
import Swal from "sweetalert2";
import localIcon from "../../../assets/images/local_login_icon.png";
// ⭐ 계정 복구 로직 함수 임포트
import { handleAccountRecover } from "../components/AccountRecoverButton";

const SignupPage = () => {
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        name: "",
        email: "",
        birth: "",
        phone: "",
        password: "",
        passwordCheck: ""
    });

    const handleChange = (e) => {
        const { id, value } = e.target;
        
        if (id === "phone") {
            const rawValue = value.replace(/[^0-9]/g, "");
            let formattedValue = "";
            if (rawValue.length < 4) formattedValue = rawValue;
            else if (rawValue.length < 8) formattedValue = `${rawValue.slice(0, 3)}-${rawValue.slice(3)}`;
            else formattedValue = `${rawValue.slice(0, 3)}-${rawValue.slice(3, 7)}-${rawValue.slice(7, 11)}`;
            
            setFormData(prev => ({ ...prev, [id]: formattedValue }));
        } else {
            setFormData(prev => ({ ...prev, [id]: value }));
        }
    };

    const { mutate, isPending } = useMutation({
        mutationFn: (submitData) => api.post("/api/user/auth/local/signup", submitData), 
        onSuccess: () => {
            Swal.fire({
                icon: 'success',
                title: '회원가입 성공',
                text: '로그인 해 주세요.',
                timer: 1500,
                showConfirmButton: false,
            }).then(() => navigate("/"));
        },
        onError: (error) => {
            const serverErrorMessage = error.response?.data?.message || "회원가입에 실패했습니다.";
            const errorCode = error.response?.data?.code;

            // ⭐ 1. 탈퇴한 계정으로 가입 시도 시 복구 유도
            if (errorCode === "USER_WITHDRAWN" || serverErrorMessage.includes("탈퇴")) {
                Swal.fire({
                    icon: 'warning',
                    title: '탈퇴된 계정입니다',
                    html: `
                        <div style="margin: 15px 0;">
                            <p>입력하신 이메일은 현재 <b>탈퇴 상태</b>입니다.</p>
                            <p style="font-size: 14px; color: #666;">다시 가입하는 대신 기존 계정을 복구하시겠습니까?</p>
                        </div>
                        <div style="display: flex; justify-content: center; gap: 10px;">
                            <button id="swal-signup-close" class="swal2-confirm swal2-styled" style="background-color: #757575; width: 100px;">취소</button>
                            <button id="swal-signup-recover" class="swal2-confirm swal2-styled" style="background-color: #f8bb86; color: #000; font-weight: bold; width: 120px;">계정 복구</button>
                        </div>
                    `,
                    showConfirmButton: false
                });

                setTimeout(() => {
                    const closeBtn = document.getElementById('swal-signup-close');
                    const recoverBtn = document.getElementById('swal-signup-recover');

                    if (closeBtn) closeBtn.onclick = () => Swal.close();
                    if (recoverBtn) {
                        recoverBtn.onclick = () => {
                            Swal.close();
                            // 현재 입력된 정보를 복구 창에 미리 채워줌
                            handleAccountRecover({
                                name: formData.name,
                                email: formData.email,
                                phone: formData.phone
                            });
                        };
                    }
                }, 100);
            } 
            // 2. 이미 존재하는 계정 (일반 중복)
            else if (errorCode === "EMAIL_ALREADY_EXISTS") {
                Swal.fire({
                    icon: 'error',
                    title: '가입 불가',
                    text: '이미 사용 중인 이메일입니다.',
                    confirmButtonColor: '#3085d6',
                });
            }
            // 3. 기타 에러
            else {
                Swal.fire({
                    icon: 'error',
                    title: '회원가입 실패',
                    text: serverErrorMessage,
                    confirmButtonColor: '#d33',
                });
            }
        }
    });

    const handleSubmit = (e) => {
        e.preventDefault();

        if (formData.password !== formData.passwordCheck) {
            Swal.fire({ icon: 'error', title: '비밀번호 불일치', text: '비밀번호가 일치하지 않습니다.', confirmButtonColor: '#d33' });
            return;
        }

        if (formData.password.length < 8) {
            Swal.fire({ icon: 'error', title: '비밀번호 형식 오류', text: '비밀번호는 최소 8자 이상이어야 합니다.', confirmButtonColor: '#d33' });
            return;
        }

        const { passwordCheck, ...submitData } = formData;
        mutate(submitData);
    };

    return (
        <div className="signupPage">
            <div className="signupCard">
                <h2 className="signupTitle">회원가입</h2>
                <p className="signupSubtitle">새 계정을 생성하고 서비스를 시작하세요.</p>

                <form className="signupForm" onSubmit={handleSubmit}>
                    <label className="fieldLabel" htmlFor="name">이름</label>
                    <input id="name" type="text" placeholder="홍길동" className="fieldInput" 
                           value={formData.name} onChange={handleChange} required />

                    <label className="fieldLabel" htmlFor="email">이메일</label>
                    <input id="email" type="email" placeholder="park@email.com" className="fieldInput" 
                           value={formData.email} onChange={handleChange} required />

                    <label className="fieldLabel" htmlFor="birth">생년월일</label>
                    <input id="birth" type="date" className="fieldInput"
                           value={formData.birth} onChange={handleChange} required />

                    <label className="fieldLabel" htmlFor="phone">전화번호</label>
                    <input id="phone" type="text" placeholder="010-1234-5678" className="fieldInput" 
                           value={formData.phone} onChange={handleChange} required />

                    <label className="fieldLabel" htmlFor="password">비밀번호</label>
                    <input id="password" type="password" placeholder="비밀번호" className="fieldInput" 
                           value={formData.password} onChange={handleChange} required />

                    <label className="fieldLabel" htmlFor="passwordCheck">비밀번호 확인</label>
                    <input id="passwordCheck" type="password" placeholder="비밀번호 확인" className="fieldInput" 
                           value={formData.passwordCheck} onChange={handleChange} required />

                    <button type="submit" className="primaryButton" disabled={isPending}>
                        {!isPending && (
                            <img src={localIcon} alt="" aria-hidden style={{ width: 20, height: 20, verticalAlign: "middle", marginRight: 6 }} />
                        )}
                        {isPending ? "가입 중..." : "회원가입"}
                    </button>
                </form>

                <div className="bottomRow">
                    <span className="bottomText">계정이 있으신가요?</span>
                    <button
                        type="button"
                        className="signupButton"
                        onClick={() => navigate("/")}
                    >
                        로그인하기
                    </button>
                </div>
            </div>
        </div>
    );
};

export default SignupPage;