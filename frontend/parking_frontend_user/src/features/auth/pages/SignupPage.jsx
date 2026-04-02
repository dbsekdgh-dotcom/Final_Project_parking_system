import React from "react";
import { useNavigate } from "react-router-dom";
import "./SignupPage.css";

const SignupPage = () => {
    const navigate = useNavigate();

    return (
        <div className="signupPage">
            <div className="signupCard">
                <h2 className="signupTitle">회원가입</h2>
                <p className="signupSubtitle">새 계정을 생성하고 서비스를 시작하세요.</p>

                <form className="signupForm">
                    <label className="fieldLabel" htmlFor="name">이름</label>
                    <input id="name" type="text" placeholder="홍길동" className="fieldInput" />

                    <label className="fieldLabel" htmlFor="email">이메일</label>
                    <input id="email" type="email" placeholder="park@email.com" className="fieldInput" />

                    <label className="fieldLabel" htmlFor="password">비밀번호</label>
                    <input id="password" type="password" placeholder="비밀번호" className="fieldInput" />

                    <label className="fieldLabel" htmlFor="passwordCheck">비밀번호 확인</label>
                    <input id="passwordCheck" type="password" placeholder="비밀번호 확인" className="fieldInput" />

                    <button type="button" className="primaryButton">회원가입</button>
                </form>

                <button type="button" className="linkButton" onClick={() => navigate("/")}>
                    로그인 화면으로 돌아가기
                </button>
            </div>
        </div>
    );
};

export default SignupPage;
