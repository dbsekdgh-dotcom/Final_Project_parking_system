import React from 'react';
import axios from "../api/axios"; 
import Swal from "sweetalert2";

export const handleAccountRecover = async (initialData = {}) => {
    const formatPhoneNumber = (value) => {
        if (!value) return value;
        const phoneNumber = value.replace(/[^\d]/g, '');
        const cpLen = phoneNumber.length;
        if (cpLen < 4) return phoneNumber;
        if (cpLen < 8) return `${phoneNumber.slice(0, 3)}-${phoneNumber.slice(3)}`;
        return `${phoneNumber.slice(0, 3)}-${phoneNumber.slice(3, 7)}-${phoneNumber.slice(7, 11)}`;
    };

    // 1단계: 정보 입력 및 인증번호 발송 요청
    const { value: info } = await Swal.fire({
        title: '계정 복구 인증',
        html: `
            <div style="text-align: left; width: 80%; margin: 0 auto; font-size: 14px; color: #666; margin-bottom: 10px;">
                * 본인 확인을 위해 전체 정보를 입력해주세요.
            </div>
            <input id="recover-name" class="swal2-input" placeholder="이름" value="${initialData.name || ''}" style="width: 80%;">
            <input id="recover-email" class="swal2-input" placeholder="이메일 전체 주소" style="width: 80%;">
            <input id="recover-phone" class="swal2-input" placeholder="전화번호" value="${initialData.phone || ''}" style="width: 80%;" maxlength="13">
        `,
        showCancelButton: true,
        confirmButtonText: '인증번호 받기',
        cancelButtonText: '취소',
        cancelButtonColor: '#6c757d',
        didOpen: () => {
            const phoneInput = document.getElementById('recover-phone');
            if (phoneInput) phoneInput.oninput = (e) => { e.target.value = formatPhoneNumber(e.target.value); };
        },
        preConfirm: () => {
            const name = document.getElementById('recover-name').value.trim();
            const email = document.getElementById('recover-email').value.trim();
            const phone = document.getElementById('recover-phone').value.trim();
            if (!name || !email || !phone) { Swal.showValidationMessage('모든 정보를 입력해주세요.'); return false; }
            return { name, email, phone };
        }
    });

    if (!info) return;

    Swal.fire({ title: '인증번호 발송 중...', didOpen: () => Swal.showLoading(), allowOutsideClick: false });

    try {
        // 2단계: 서버에 인증번호 발송 요청
        await axios.post('/api/user/auth/local/send-recover-code', info);

        // 3단계: 인증번호 입력 (버튼을 '확인'으로 변경)
        const { value: authCode } = await Swal.fire({
            title: '인증번호 입력',
            text: '이메일로 전송된 6자리 인증번호를 입력해주세요.',
            input: 'text',
            inputAttributes: { maxlength: 6 },
            showCancelButton: true,
            confirmButtonText: '확인',
            cancelButtonText: '취소',
            cancelButtonColor: '#6c757d',
            preConfirm: (code) => {
                if (!code || code.length !== 6) { Swal.showValidationMessage('인증번호 6자리를 입력해주세요.'); }
                return code;
            }
        });

        if (!authCode) return;

        // 4단계: 새 비밀번호 설정 모달 (인증 성공 후 단계)
        const { value: passwords } = await Swal.fire({
            title: '새 비밀번호 설정',
            html: `
                <input type="password" id="new-password" class="swal2-input" placeholder="새 비밀번호 (8자 이상)" style="width: 80%;">
                <input type="password" id="password-confirm" class="swal2-input" placeholder="비밀번호 확인" style="width: 80%;">
            `,
            showCancelButton: true,
            confirmButtonText: '복구 완료',
            cancelButtonText: '취소',
            cancelButtonColor: '#6c757d',
            preConfirm: () => {
                const newPassword = document.getElementById('new-password').value;
                const passwordConfirm = document.getElementById('password-confirm').value;
                if (!newPassword || newPassword.length < 8) {
                    Swal.showValidationMessage('비밀번호는 8자 이상이어야 합니다.');
                    return false;
                }
                if (newPassword !== passwordConfirm) {
                    Swal.showValidationMessage('비밀번호가 일치하지 않습니다.');
                    return false;
                }
                return { newPassword, passwordConfirm };
            }
        });

        if (!passwords) return;

        // 5단계: 최종 복구 API 호출 (모든 데이터 합쳐서 전송)
        await axios.post("/api/user/auth/local/recover", { 
            email: info.email, 
            name: info.name,
            phone: info.phone,
            authCode: authCode,
            newPassword: passwords.newPassword,
            passwordConfirm: passwords.passwordConfirm
        });

        await Swal.fire({
            icon: 'success',
            title: '복구 성공',
            text: '비밀번호가 재설정되었습니다. 다시 로그인해주세요.',
        });

        window.location.href = '/';

    } catch (error) {
        Swal.fire({
            icon: 'error',
            title: '오류 발생',
            text: error.response?.data?.message || '인증번호가 틀렸거나 복구 중 오류가 발생했습니다.'
        });
    }
};

const AccountRecoverButton = ({ className, style, buttonText = "계정 복구", initialData = {} }) => {
    return (
        <button type="button" className={className} style={style} onClick={() => handleAccountRecover(initialData)}>
            {buttonText}
        </button>
    );
};

export default AccountRecoverButton;