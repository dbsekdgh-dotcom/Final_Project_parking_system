import axios from "../api/axios";
import React from 'react';
import { useNavigate } from 'react-router-dom';
import Swal from 'sweetalert2';

const WithdrawButton = () => {
    const navigate = useNavigate();

    const handleWithdraw = async () => {
        const { value: formValues } = await Swal.fire({
            title: '정말로 탈퇴하시겠습니까?',
            html: `
                <p style="color: #ff4d4f; font-size: 0.9rem; margin-bottom: 20px;">
                    탈퇴 시 소셜 연동 정보가 삭제되며, 계정은 복구할 수 없습니다.
                </p>
                <input id="swal-input1" class="swal2-input" type="password" placeholder="비밀번호">
                <input id="swal-input2" class="swal2-input" type="password" placeholder="비밀번호 확인">
                <input id="swal-input3" class="swal2-input" placeholder="'회원 탈퇴'를 입력해주세요">
            `,
            focusConfirm: false,
            showCancelButton: true,
            confirmButtonText: '탈퇴하기',
            cancelButtonText: '취소',
            cancelButtonColor: '#6c757d',
            confirmButtonColor: '#d33',
            preConfirm: () => {
                const password = document.getElementById('swal-input1').value;
                const confirmPassword = document.getElementById('swal-input2').value;
                const confirmText = document.getElementById('swal-input3').value;

                // 1. 필수 입력 체크
                if (!password || !confirmPassword || !confirmText) {
                    Swal.showValidationMessage('모든 항목을 입력해주세요.');
                    return false;
                }
                // 2. 비밀번호 일치 확인
                if (password !== confirmPassword) {
                    Swal.showValidationMessage('비밀번호가 일치하지 않습니다.');
                    return false;
                }
                // 3. 탈퇴 문구 확인
                if (confirmText !== '회원 탈퇴') {
                    Swal.showValidationMessage("'회원 탈퇴'를 정확히 입력해주세요.");
                    return false;
                }

                return { password, confirmPassword, confirmText };
            }
        });

        if (formValues) {
            try {
                // localStorage에서 토큰 가져오기 (키 이름이 'accessToken'인지 확인하세요!)
                const token = localStorage.getItem('accessToken');

                const response = await axios.delete('/api/user/auth/local/withdraw', {
                    data: formValues, // DELETE 요청 시 body 데이터는 data 키에 넣어야 함
                    headers: {
                        Authorization: `Bearer ${token}` // 서버 인증을 위한 토큰 전송
                    }
                });

                if (response.status === 200) {
                    Swal.fire({
                        icon: 'success',
                        title: '탈퇴 성공',
                        text: '그동안 이용해 주셔서 감사합니다.',
                        timer: 2000,
                        showConfirmButton: false
                    }).then(() => {
                        localStorage.clear(); // 모든 인증 정보 삭제
                        navigate('/');    // 로그인 페이지로 이동
                    });
                }
            } catch (error) {
                console.error("탈퇴 요청 에러:", error);
                const errorMsg = error.response?.data?.message || '비밀번호가 틀렸거나 오류가 발생했습니다.';
                Swal.fire('탈퇴 실패', errorMsg, 'error');
            }
        }
    };

    return (
        <div 
            onClick={handleWithdraw}
            className='menu-item withdraw-menu'
            style={{
                color: '#e74c3c', 
                cursor: 'pointer', 
                padding: '10px 20px',
                fontSize: '14px',
                display: 'flex',
                alignItems: 'center',
                fontWeight: '500'
            }}    
        >
            <span style={{ marginRight: '8px' }}>🚪</span> 회원 탈퇴
        </div>
    );
}

export default WithdrawButton;