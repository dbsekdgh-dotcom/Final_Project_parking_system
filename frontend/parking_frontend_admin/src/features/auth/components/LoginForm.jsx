import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom';
import { loginAdmin } from '../api/authApi';

const LoginForm = () => {
    const [loginId,setLoginId] = useState('');
    const [password,setPassword] = useState('');
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        try {
            const data = await loginAdmin({loginId,password});

            console.log("로그인 응답 데이터: ",data);

            sessionStorage.setItem('accessToken',data.accessToken);
            sessionStorage.setItem('adminId',data.adminId);
            localStorage.setItem('adminName',data.adminName);
            navigate('/admin/dashboard');
        } catch (error){
            alert('로그인에 실패했습니다. 아이디 또는 비밀번호를 확인해주세요.');
        }
    }

    return (
        <form className='login-form' onSubmit={handleLogin}>
            <div className='input-group'>
                <label>아이디</label>
                <input type="text" placeholder='아이디를 입력하세요.' value={loginId} onChange={(e)=>setLoginId(e.target.value)} required/>
            </div>
            <div className='input-group'>
                <label>비밀번호</label>
                <input type="password" placeholder='********' value={password} onChange={(e)=>setPassword(e.target.value)} required/>
            </div>
            <button type='submit' className='login-btn'>로그인</button>
        </form>
    )
}

export default LoginForm