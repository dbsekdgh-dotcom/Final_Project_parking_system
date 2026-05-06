import React, { useState, useRef } from "react";
import { useNavigate } from "react-router-dom";
import "./SignupPage.css";
import { useMutation } from "@tanstack/react-query";
import api from "../api/axios";
import Swal from "sweetalert2";
import localIcon from "../../../assets/images/local_login_icon.png";

// 유틸리티 및 상수 임포트
import { handleLocalRecover } from '../utils/accountUtils.js';
import { AUTH_ERROR_CODES } from '../constants/errorCodes';

// 분리된 컴포넌트 임포트
import EmailInputField from "../components/EmailInputField";
import PasswordField from "../components/PasswordField";

const SignupPage = () => {
    const navigate = useNavigate();

    // 이메일 확정 상태 관리 (중복 확인 통과 여부)
    const [isEmailFixed, setIsEmailFixed] = useState(false);

    // 생년월일 분리 입력용 ref (년 → 월 → 일 자동 포커스)
    const birthMonthRef = useRef(null);
    const birthDayRef = useRef(null);
    const [birthParts, setBirthParts] = useState({ year: '', month: '', day: '' });

    const [formData, setFormData] = useState({
        name: "",
        email: "",
        birth: "",
        phone: "",
        password: "",
        passwordCheck: ""
    });

    // 이메일 변경 시 호출 (EmailInputField용)
    const setEmail = (newEmail) => {
        setFormData(prev => ({ ...prev, email: newEmail }));
    };

    // 생년월일 분리 입력 핸들러 (년 4자리, 월 2자리, 일 2자리)
    const handleBirthChange = (e) => {
        const { name, value } = e.target;
        const digits = value.replace(/\D/g, ''); // 숫자만 허용

        const newParts = { ...birthParts };
        if (name === 'birthYear') {
            newParts.year = digits.slice(0, 4);
            if (newParts.year.length === 4) birthMonthRef.current?.focus();
        } else if (name === 'birthMonth') {
            newParts.month = digits.slice(0, 2);
            if (newParts.month.length === 2) birthDayRef.current?.focus();
        } else {
            newParts.day = digits.slice(0, 2);
        }

        setBirthParts(newParts);

        // 세 파트가 모두 채워지면 YYYY-MM-DD 형식으로 formData에 반영
        const y = newParts.year, m = newParts.month, d = newParts.day;
        const combined = (y.length === 4 && m.length >= 1 && d.length >= 1)
            ? `${y}-${m.padStart(2, '0')}-${d.padStart(2, '0')}`
            : '';
        setFormData(prev => ({ ...prev, birth: combined }));
    };

    // 일반 입력 변경 핸들러
    const handleChange = (e) => {
        const { id, value } = e.target;
        
        if (id === "phone") {
            // 전화번호 자동 포맷팅 로직
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

    // 회원가입 API 호출을 위한 Mutation
    const { mutate, isPending } = useMutation({
        mutationFn: (submitData) => api.post("/api/user/auth/local/signup", submitData), 
        onSuccess: () => {
            Swal.fire({
                icon: 'success',
                title: '회원가입 성공',
                text: '환영합니다! 로그인을 진행해주세요.',
                confirmButtonText: '확인',
                confirmButtonColor: '#3085d6',
            }).then(() => navigate("/"));
        },
        onError: async (error) => {
            const serverErrorMessage = error.response?.data?.message || "회원가입에 실패했습니다.";
            const errorCode = error.response?.data?.errorCode; // 서버 응답 키값에 맞춰 errorCode 확인

            // [중요] 가입 시도 중에도 탈퇴 계정이 확인될 경우 복구 모달 실행
            if (errorCode === AUTH_ERROR_CODES.WITHDRAWN_ACCOUNT) {
                await handleLocalRecover(formData.email);
            } else if (errorCode === AUTH_ERROR_CODES.EMAIL_DUPLICATE) {
                Swal.fire({ icon: 'error', title: '가입 불가', text: '이미 사용 중인 이메일입니다.', confirmButtonText: '확인', confirmButtonColor: '#3085d6' });
                setIsEmailFixed(false); // 중복이면 고정 해제
            } else {
                Swal.fire({ icon: 'error', title: '회원가입 실패', text: serverErrorMessage, confirmButtonText: '확인', confirmButtonColor: '#d33' });
            }
        }
    });

    // 폼 제출 핸들러
    const handleSubmit = (e) => {
        e.preventDefault();

        // 1. 이메일 중복확인 체크 여부 확인
        if (!isEmailFixed) {
            Swal.fire({ icon: 'warning', title: '중복 확인 필요', text: '이메일 중복 확인을 완료해 주세요.', confirmButtonText: '확인', confirmButtonColor: '#3085d6' });
            return;
        }

        // 2. 비밀번호 유효성 체크
        if (formData.password !== formData.passwordCheck) {
            Swal.fire({ icon: 'error', title: '비밀번호 불일치', text: '비밀번호가 일치하지 않습니다.', confirmButtonText: '확인', confirmButtonColor: '#d33' });
            return;
        }

        if (formData.password.length < 8) {
            Swal.fire({ icon: 'error', title: '비밀번호 형식 오류', text: '비밀번호는 최소 8자 이상이어야 합니다.', confirmButtonText: '확인', confirmButtonColor: '#d33' });
            return;
        }

        // 비밀번호 확인 필드를 제외하고 전송
        const { passwordCheck, ...submitData } = formData;
        mutate(submitData);
    };

    return (
        <div className="signupPage">
            <div className="signupCard">
                <h2 className="signupTitle">회원가입</h2>
                <p className="signupSubtitle">새 계정을 생성하고 서비스를 시작하세요.</p>

                <form className="signupForm" onSubmit={handleSubmit}>
                    {/* 이름 입력 */}
                    <label className="fieldLabel" htmlFor="name">이름</label>
                    <input 
                        id="name" 
                        type="text" 
                        placeholder="홍길동" 
                        className="fieldInput" 
                        value={formData.name} 
                        onChange={handleChange} 
                        required 
                    />

                    {/* 이메일 입력 (중복 확인 로직 포함 컴포넌트) */}
                    <EmailInputField 
                        email={formData.email}
                        setEmail={setEmail}
                        isEmailFixed={isEmailFixed}
                        setIsEmailFixed={setIsEmailFixed}
                    />

                    {/* 생년월일 입력 (년/월/일 분리 — 4자리 입력 시 자동 포커스 이동) */}
                    <label className="fieldLabel">생년월일</label>
                    <div style={{ display: 'flex', gap: '6px', alignItems: 'center' }}>
                        <input
                            name="birthYear"
                            type="text"
                            inputMode="numeric"
                            placeholder="YYYY"
                            maxLength={4}
                            className="fieldInput"
                            style={{ flex: 2, textAlign: 'center' }}
                            value={birthParts.year}
                            onChange={handleBirthChange}
                            required
                        />
                        <span style={{ color: '#aaa' }}>-</span>
                        <input
                            ref={birthMonthRef}
                            name="birthMonth"
                            type="text"
                            inputMode="numeric"
                            placeholder="MM"
                            maxLength={2}
                            className="fieldInput"
                            style={{ flex: 1, textAlign: 'center' }}
                            value={birthParts.month}
                            onChange={handleBirthChange}
                            required
                        />
                        <span style={{ color: '#aaa' }}>-</span>
                        <input
                            ref={birthDayRef}
                            name="birthDay"
                            type="text"
                            inputMode="numeric"
                            placeholder="DD"
                            maxLength={2}
                            className="fieldInput"
                            style={{ flex: 1, textAlign: 'center' }}
                            value={birthParts.day}
                            onChange={handleBirthChange}
                            required
                        />
                    </div>

                    {/* 전화번호 입력 */}
                    <label className="fieldLabel" htmlFor="phone">전화번호</label>
                    <input 
                        id="phone" 
                        type="text" 
                        placeholder="010-1234-5678" 
                        className="fieldInput" 
                        value={formData.phone} 
                        onChange={handleChange} 
                        required 
                    />

                    {/* 비밀번호 입력 */}
                    <PasswordField 
                        id="password" 
                        label="비밀번호" 
                        placeholder="8자 이상의 비밀번호"
                        value={formData.password}
                        onChange={handleChange}
                    />

                    {/* 비밀번호 확인 입력 */}
                    <PasswordField 
                        id="passwordCheck" 
                        label="비밀번호 확인" 
                        placeholder="비밀번호를 한 번 더 입력하세요"
                        value={formData.passwordCheck}
                        onChange={handleChange}
                    />

                    {/* 제출 버튼 */}
                    <button type="submit" className="primaryButton" disabled={isPending || !isEmailFixed}>
                        {!isPending && (
                            <img src={localIcon} alt="" aria-hidden style={{ width: 20, height: 20, verticalAlign: "middle", marginRight: 6 }} />
                        )}
                        {isPending ? "가입 처리 중..." : "회원가입"}
                    </button>
                </form>

                <div className="bottomRow">
                    <span className="bottomText">계정이 이미 있으신가요?</span>
                    <button type="button" className="signupButton" onClick={() => navigate("/")}>로그인</button>
                </div>
            </div>
        </div>
    );
};

export default SignupPage;