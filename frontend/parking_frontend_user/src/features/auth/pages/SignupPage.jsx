import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./SignupPage.css";
import { useMutation } from "@tanstack/react-query";
import api from "../api/axios";

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
            alert("회원가입 성공!! 로그인 해 주세요.");
            navigate("/");
        },
        onError: (error) => {
            const serverErrorMessage = error.response?.data?.message || error.response?.data || "회원가입 실패!!";
            alert(serverErrorMessage);
        }
    });

    const handleSubmit = (e) => {
        e.preventDefault();

        if (formData.password !== formData.passwordCheck) {
            alert("비밀번호가 일치하지 않습니다.");
            return;
        }

        if (formData.password.length < 8) {
            alert("비밀번호는 최소 8자 이상이어야 합니다.")
            return
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
                        {isPending ? "가입 중..." : "회원가입"}
                    </button>
                </form>
            </div>
        </div>
    );
};

export default SignupPage;