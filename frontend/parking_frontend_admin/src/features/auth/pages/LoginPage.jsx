import React from 'react'
import LoginForm from '../components/LoginForm'
import './LoginPage.css'

const LoginPage = () => {
    return (
        <div className='admin-login-container'>
            {/* 상단 로고 바 */}
            <header className='admin-login-header'>
                <div className='logo-wrapper'>
                    <svg width="35" height="35" viewBox="0 0 24 24" fill="none">
                        <path d="M4 4h6v6H4V4zm10 0h6v6h-6V4zM4 14h6v6H4v-6zm10 0h6v6h-6v-6z" fill="#fff" opacity="0.95" />
                    </svg>
                    <span className='logo-text'>Parking</span>
                </div>
            </header>
            {/* 중앙 로그인 박스 */}
            <main className='admin-login-content'>
                <div className='login-box'>
                    <div className='login-intro'>
                        <h1>로그인</h1>
                        <p>계정에 로그인하여 서비스를 이용하세요.</p>
                    </div>
                    <LoginForm />
                </div>
            </main>
        </div>
    );
}

export default LoginPage