import axios from "../api/axios";
import React from 'react'
import { useNavigate } from 'react-router-dom'
import Swal from 'sweetalert2';

const FindAccountButtons = () => {
    const navigate = useNavigate();

    const formatPhoneNumber = (value) => {
        if (!value) return value;
        const phoneNumber = value.replace(/[^\d]/g, '');
        const cpLen = phoneNumber.length;
        if (cpLen < 4) return phoneNumber;
        if (cpLen < 8) return `${phoneNumber.slice(0, 3)}-${phoneNumber.slice(3)}`;
        return `${phoneNumber.slice(0, 3)}-${phoneNumber.slice(3, 7)}-${phoneNumber.slice(7, 11)}`;
    };

    const openFindEmailModal = async () => {
        const { value: formValues } = await Swal.fire({
            title: '아이디(이메일) 찾기',
            html: `
                <input id="swal-name" class="swal2-input" placeholder="이름을 입력하세요" style="width: 80%;">
                <input id="swal-phone" class="swal2-input" placeholder="휴대폰 번호를 입력하세요" style="width:80%;" maxlength="13">     
            `,
            showCancelButton: true,
            confirmButtonText: '찾기',
            cancelButtonText: '취소', // 한글화
            confirmButtonColor: '#3085d6',
            didOpen: () => {
                const phoneInput = document.getElementById('swal-phone');
                phoneInput.oninput = (e) => {
                    e.target.value = formatPhoneNumber(e.target.value);
                };
            },
            preConfirm: () => {
                const name = document.getElementById('swal-name').value;
                const phone = document.getElementById('swal-phone').value;
                if (!name || !phone) {
                    Swal.showValidationMessage('모든 정보를 입력해주세요.');
                }
                return { name, phone }
            }
        });

        if (formValues) {
            try {
                const res = await axios.post("/api/user/auth/local/find-email", formValues);
                const maskenEmail = res.data.email;

                Swal.fire({
                    icon: 'success',
                    title: '계정을 찾았습니다!',
                    html: `
                        <div style="margin: 20px 0;">
                            고객님의 이메일 주소는<br>
                            <b style="font-size: 1.2rem; color: #3085d6;">${maskenEmail}</b> 입니다.
                        </div>
                        <div style="display: flex; justify-content: center; gap: 10px;">
                            <button id="go-login" class="swal2-confirm swal2-styled" style="background-color: #3085d6; margin: 0;">로그인하기</button>
                            <button id="go-find-pw" class="swal2-confirm swal2-styled" style="background-color: #757575; margin: 0;">비밀번호 찾기</button>
                        </div>
                    `,
                    showConfirmButton: false
                });

                // 커스텀 버튼 이벤트 연결
                setTimeout(() => {
                    const loginBtn = document.getElementById('go-login');
                    const pwBtn = document.getElementById('go-find-pw');
                    if(loginBtn) loginBtn.onclick = () => Swal.close();
                    if(pwBtn) pwBtn.onclick = () => { Swal.close(); openFindPwModal(); };
                }, 100);

            } catch (error) {
                Swal.fire({
                    title: '실패',
                    text: '일치하는 정보가 없습니다.',
                    icon: 'error',
                    confirmButtonText: '확인' // 한글화
                });
            }
        }
    };

    const openFindPwModal = async () => {
        const { value: info } = await Swal.fire({
            title: '비밀번호 찾기',
            text: '가입하신 정보를 입력해주세요.',
            html: `
                <input id="pw-name" class="swal2-input" placeholder="이름" style="width: 80%;">
                <input id="pw-email" class="swal2-input" placeholder="이메일" style="width: 80%;">
                <input id="pw-phone" class="swal2-input" placeholder="전화번호" style="width: 80%;" maxlength="13">
            `,
            showCancelButton: true,
            confirmButtonText: '인증번호 받기',
            cancelButtonText: '취소', // 한글화
            didOpen: () => {
                const phoneInput = document.getElementById('pw-phone');
                phoneInput.oninput = (e) => {
                    e.target.value = formatPhoneNumber(e.target.value);
                };
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

        Swal.fire({
            title: '인증번호 발송 중',
            text: '잠시만 기다려주세요...',
            allowOutsideClick: false, // 로딩 중 창 밖 클릭 방지
            didOpen: () => {
                Swal.showLoading(); // SweetAlert2 기본 로딩 애니메이션 실행
            }
        });

        try {
            await axios.post("/api/user/auth/local/send-code", info);
            
            const { value: code } = await Swal.fire({
                title: '인증번호 입력',
                text: '이메일로 발송된 6자리 번호를 입력하세요.',
                input: 'text',
                inputAttributes: { maxlength: 6 },
                showCancelButton: true,
                confirmButtonText: '인증하기',
                cancelButtonText: '취소', // 한글화
            });

            if (!code) return;

            await axios.post("/api/user/auth/local/verify-code", { email: info.email, code });

            const { value: passwordValues } = await Swal.fire({
                title: '새 비밀번호 설정',
                html: `
                    <input type="password" id="new-pw" class="swal2-input" placeholder="새 비밀번호" style="width: 80%;">
                    <input type="password" id="confirm-pw" class="swal2-input" placeholder="비밀번호 확인" style="width: 80%;">
                `,
                showCancelButton: true,
                confirmButtonText: '변경하기',
                cancelButtonText: '취소', // 한글화
                preConfirm: () => {
                    const newPassword = document.getElementById('new-pw').value;
                    const confirmPassword = document.getElementById('confirm-pw').value;
                    if (newPassword !== confirmPassword) {
                        Swal.showValidationMessage('비밀번호가 일치하지 않습니다.');
                    }
                    return { newPassword, confirmPassword }
                }
            });

            if (!passwordValues) return;

            await axios.post("/api/user/auth/local/reset-password", {
                email: info.email,
                newPassword: passwordValues.newPassword,
                confirmPassword: passwordValues.confirmPassword
            });

            Swal.fire({
                title: '성공',
                text: '비밀번호가 성공적으로 변경되었습니다.',
                icon: 'success',
                confirmButtonText: '확인' // 한글화
            });
        } catch (error) {
            const errorMsg = error.response?.data?.message || '처리에 실패했습니다.';
            Swal.fire({
                title: '에러',
                text: errorMsg,
                icon: 'error',
                confirmButtonText: '확인' // 한글화
            });
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
    )
}

export default FindAccountButtons;