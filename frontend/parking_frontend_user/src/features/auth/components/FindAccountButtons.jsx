import React from 'react';
import { useNavigate } from 'react-router-dom';
import Swal from 'sweetalert2';
import axios from "../api/axios";
// ⭐ 방금 만든 복구 로직 함수를 가져옵니다.
import { handleAccountRecover } from './AccountRecoverButton'; 

const FindAccountButtons = () => {
    const navigate = useNavigate();

    // 휴대폰 번호 포맷팅 (010-0000-0000)
    const formatPhoneNumber = (value) => {
        if (!value) return value;
        const phoneNumber = value.replace(/[^\d]/g, '');
        const cpLen = phoneNumber.length;
        if (cpLen < 4) return phoneNumber;
        if (cpLen < 8) return `${phoneNumber.slice(0, 3)}-${phoneNumber.slice(3)}`;
        return `${phoneNumber.slice(0, 3)}-${phoneNumber.slice(3, 7)}-${phoneNumber.slice(7, 11)}`;
    };

    // 아이디(이메일) 찾기 모달
    const openFindEmailModal = async () => {
        const { value: formValues } = await Swal.fire({
            title: '아이디(이메일) 찾기',
            html: `
                <input id="swal-name" class="swal2-input" placeholder="이름을 입력하세요" style="width: 80%;">
                <input id="swal-phone" class="swal2-input" placeholder="휴대폰 번호를 입력하세요" style="width:80%;" maxlength="13">     
            `,
            showCancelButton: true,
            confirmButtonText: '찾기',
            cancelButtonText: '취소',
            cancelButtonColor: '#6c757d',
            confirmButtonColor: '#3085d6',
            didOpen: () => {
                const phoneInput = document.getElementById('swal-phone');
                if (phoneInput) {
                    phoneInput.oninput = (e) => {
                        e.target.value = formatPhoneNumber(e.target.value);
                    };
                }
            },
            preConfirm: () => {
                const name = document.getElementById('swal-name').value;
                const phone = document.getElementById('swal-phone').value;
                if (!name || !phone) {
                    Swal.showValidationMessage('모든 정보를 입력해주세요.');
                }
                return { name, phone };
            }
        });

        if (formValues) {
            try {
                const res = await axios.post("/api/user/auth/local/find-email", formValues);
                const { email, isWithdrawn } = res.data;

                if (isWithdrawn === true) {
                    Swal.fire({
                        icon: 'warning',
                        title: '탈퇴된 계정입니다',
                        html: `
                            <div style="margin: 20px 0;">
                                <p style="color: #666;">현재 <b>탈퇴 상태</b>인 계정입니다.</p>
                                <b style="font-size: 1.2rem; color: #d33;">${email}</b>
                            </div>
                            <button id="go-recover-now" class="swal2-confirm swal2-styled" style="background-color: #f8bb86; color: #000; font-weight: bold;">지금 계정 복구하기</button>
                        `,
                        showConfirmButton: false
                    });

                    setTimeout(() => {
                        const btn = document.getElementById('go-recover-now');
                        if (btn) {
                            btn.onclick = () => {
                                Swal.close();
                                handleAccountRecover({
                                    name: formValues.name,
                                    email: email,
                                    phone: formValues.phone
                                });
                            };
                        }
                    }, 100);

                } else {
                    Swal.fire({
                        icon: 'success',
                        title: '계정을 찾았습니다!',
                        html: `
                            <div style="margin: 20px 0;">
                                <p style="color: #666; margin-bottom: 10px;">고객님의 이메일 주소입니다.</p>
                                <b style="font-size: 1.2rem; color: #3085d6;">${email}</b>
                            </div>
                            <div style="display: flex; justify-content: center; gap: 10px;">
                                <button id="go-login" class="swal2-confirm swal2-styled" style="background-color: #3085d6; margin: 0; width: 110px; height: 38px; padding: 0; font-size: 14px; line-height: 38px;">로그인하기</button>
                                <button id="go-find-pw" class="swal2-confirm swal2-styled" style="background-color: #757575; margin: 0; width: 110px; height: 38px; padding: 0; font-size: 14px; line-height: 38px;">비번 찾기</button>
                            </div>
                        `,
                        showConfirmButton: false
                    });

                    setTimeout(() => {
                        const loginBtn = document.getElementById('go-login');
                        const pwBtn = document.getElementById('go-find-pw');
                        if (loginBtn) loginBtn.onclick = () => Swal.close();
                        if (pwBtn) {
                            pwBtn.onclick = () => {
                                Swal.close();
                                openFindPwModal();
                            };
                        }
                    }, 100);
                }

            } catch (error) {
                Swal.fire('실패', error.response?.data?.message || '일치하는 정보가 없습니다.', 'error');
            }
        }
    };

    // 비밀번호 찾기 모달 (문법 교정됨)
    const openFindPwModal = async () => {
        const { value: info } = await Swal.fire({
            title: '비밀번호 찾기',
            html: `
                <input id="pw-name" class="swal2-input" placeholder="이름" style="width: 80%;">
                <input id="pw-email" class="swal2-input" placeholder="이메일" style="width: 80%;">
                <input id="pw-phone" class="swal2-input" placeholder="전화번호" style="width: 80%;" maxlength="13">
            `,
            showCancelButton: true,
            confirmButtonText: '인증번호 받기',
            cancelButtonText: '취소',
            cancelButtonColor: '#6c757d',
            didOpen: () => {
                const phoneInput = document.getElementById('pw-phone');
                if (phoneInput) {
                    phoneInput.oninput = (e) => { e.target.value = formatPhoneNumber(e.target.value); };
                }
            },
            preConfirm: () => {
                const name = document.getElementById('pw-name').value;
                const email = document.getElementById('pw-email').value;
                const phone = document.getElementById('pw-phone').value;
                if (!name || !email || !phone) {
                    Swal.showValidationMessage('모든 정보를 입력해주세요.');
                }
                return { name, email, phone };
            }
        });

        if (!info) return;

        Swal.fire({ title: '발송 중...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });

        try {
            // 인증번호 발송
            await axios.post("/api/user/auth/local/send-code", info);
            
            const { value: code } = await Swal.fire({
                title: '인증번호 입력',
                input: 'text',
                inputAttributes: { maxlength: 6 },
                showCancelButton: true,
                confirmButtonText: '인증하기',
                cancelButtonText: '취소',
                cancelButtonColor: '#6c757d',
            });

            if (!code) return;

            // 인증번호 검증
            await axios.post("/api/user/auth/local/verify-code", { email: info.email, code });

            // 비밀번호 재설정
            const { value: pwValues } = await Swal.fire({
                title: '새 비밀번호 설정',
                html: `
                    <input type="password" id="new-pw" class="swal2-input" placeholder="새 비밀번호" style="width: 80%;">
                    <input type="password" id="confirm-pw" class="swal2-input" placeholder="비밀번호 확인" style="width: 80%;">
                `,
                showCancelButton: true,
                confirmButtonText: '변경하기',
                cancelButtonText: '취소',
                cancelButtonColor: '#6c757d',
                preConfirm: () => {
                    const newPassword = document.getElementById('new-pw').value;
                    const confirmPassword = document.getElementById('confirm-pw').value;
                    if (newPassword !== confirmPassword) {
                        Swal.showValidationMessage('비밀번호가 일치하지 않습니다.');
                        return false;
                    }
                    return { newPassword, confirmPassword };
                }
            });

            if (!pwValues) return;

            await axios.post("/api/user/auth/local/reset-password", {
                email: info.email,
                newPassword: pwValues.newPassword,
                confirmPassword: pwValues.confirmPassword
            });

            Swal.fire('성공', '비밀번호가 성공적으로 변경되었습니다.', 'success');
        } catch (error) {
            Swal.fire('에러', error.response?.data?.message || '요청 처리에 실패했습니다.', 'error');
        }
    };

    return (
        <div className="bottomRow" style={{ display: 'flex', justifyContent: 'center', gap: '10px', marginTop: '20px', fontSize: '14px', color: '#666' }}>
            <button type="button" onClick={openFindEmailModal} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'inherit' }}>
                아이디 찾기
            </button>
            <span>|</span>
            <button type="button" onClick={openFindPwModal} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'inherit' }}>
                비밀번호 찾기
            </button>
        </div>
    );
};

export default FindAccountButtons;