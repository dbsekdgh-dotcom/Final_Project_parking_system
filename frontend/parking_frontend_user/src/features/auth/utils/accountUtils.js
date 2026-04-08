import Swal from 'sweetalert2';
import api from '../api/axios';

// 1. 휴대폰 번호 포맷팅 도구
export const formatPhoneNumber = (value) => {
    if (!value) return value;
    const phoneNumber = value.replace(/[^\d]/g, '');
    const cpLen = phoneNumber.length;
    if (cpLen < 4) return phoneNumber;
    if (cpLen < 8) return `${phoneNumber.slice(0, 3)}-${phoneNumber.slice(3)}`;
    return `${phoneNumber.slice(0, 3)}-${phoneNumber.slice(3, 7)}-${phoneNumber.slice(7, 11)}`;
};

/**
 * 2. 계정 복구 로직 (탈퇴 계정용)
 */
export const handleAccountRecover = async (userData) => {
    const { value: confirm } = await Swal.fire({
        title: '계정 복구',
        text: `${userData.email} 계정을 복구하시겠습니까?`,
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: '복구하기',
        confirmButtonColor: '#3085d6',
        cancelButtonText: '취소'
    });

    if (confirm) {
        try {
            Swal.fire({ title: '처리 중...', didOpen: () => Swal.showLoading() });
            await api.post('/api/user/auth/local/recover', { email: userData.email });
            Swal.fire('성공', '계정이 성공적으로 복구되었습니다. 이제 로그인할 수 있습니다.', 'success');
        } catch (error) {
            Swal.fire('에러', error.response?.data?.message || '복구 처리에 실패했습니다.', 'error');
        }
    }
};

/**
 * 3. 아이디(이메일) 찾기 모달
 */
export const openFindEmailModal = async () => {
    const { value: formValues } = await Swal.fire({
        title: '아이디(이메일) 찾기',
        html: `
            <input id="swal-name" class="swal2-input" placeholder="이름을 입력하세요" style="width: 80%;">
            <input id="swal-phone" class="swal2-input" placeholder="휴대폰 번호를 입력하세요" style="width:80%;" maxlength="13">     
        `,
        showCancelButton: true,
        confirmButtonText: '찾기',
        cancelButtonText: '취소',
        didOpen: () => {
            const phoneInput = document.getElementById('swal-phone');
            if (phoneInput) phoneInput.oninput = (e) => { e.target.value = formatPhoneNumber(e.target.value); };
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
            const res = await api.post("/api/user/auth/local/find-email", formValues);
            const { email, isWithdrawn } = res.data;

            if (isWithdrawn) {
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
                    if (btn) btn.onclick = () => { Swal.close(); handleAccountRecover({ email }); };
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
                            <button id="go-login" class="swal2-confirm swal2-styled" style="background-color: #3085d6; width: 110px; font-size: 14px;">로그인하기</button>
                            <button id="go-find-pw" class="swal2-confirm swal2-styled" style="background-color: #757575; width: 110px; font-size: 14px;">비번 찾기</button>
                        </div>
                    `,
                    showConfirmButton: false
                });

                setTimeout(() => {
                    document.getElementById('go-login').onclick = () => Swal.close();
                    document.getElementById('go-find-pw').onclick = () => { Swal.close(); openFindPwModal(email); };
                }, 100);
            }
        } catch (error) {
            Swal.fire('실패', error.response?.data?.message || '일치하는 정보가 없습니다.', 'error');
        }
    }
};

/**
 * 4-1. [신규] 이메일 인증 전용 모달 (계정 잠금 해제/본인 확인용)
 */
export const openEmailVerificationModal = async (initialEmail = "") => {
    const { value: info } = await Swal.fire({
        title: '이메일 인증하기',
        html: `
            <div style="text-align: left; width: 80%; margin: 0 auto; font-size: 13px; color: #888; margin-bottom: 10px;">
                본인 확인을 위해 가입 정보를 입력해주세요.
            </div>
            <input id="verify-name" class="swal2-input" placeholder="이름" style="width: 80%;">
            <input id="verify-email" class="swal2-input" placeholder="이메일" style="width: 80%;" value="${initialEmail}">
            <input id="verify-phone" class="swal2-input" placeholder="전화번호" style="width: 80%;" maxlength="13">
        `,
        showCancelButton: true,
        confirmButtonText: '인증번호 받기',
        cancelButtonText: '취소',
        confirmButtonColor: '#3085d6',
        cancelButtonColor: '#6e7881',
        didOpen: () => {
            const phoneInput = document.getElementById('verify-phone');
            if (phoneInput) phoneInput.oninput = (e) => { e.target.value = formatPhoneNumber(e.target.value); };
        },
        preConfirm: () => {
            const name = document.getElementById('verify-name').value;
            const email = document.getElementById('verify-email').value;
            const phone = document.getElementById('verify-phone').value;
            if (!name || !email || !phone) {
                Swal.showValidationMessage('모든 정보를 입력해주세요.');
            }
            return { name, email, phone };
        }
    });

    if (info) await handlePasswordProcess(info.email, info, 'VERIFY');
};

/**
 * 4-2. 비밀번호 찾기 모달 (기존 로직 유지)
 */
export const openFindPwModal = async (initialEmail = "") => {
    const { value: info } = await Swal.fire({
        title: '비밀번호 찾기',
        html: `
            <div style="text-align: left; width: 80%; margin: 0 auto; font-size: 13px; color: #888; margin-bottom: 10px;">
                본인 확인을 위해 가입 정보를 입력해주세요.
            </div>
            <input id="pw-name" class="swal2-input" placeholder="이름" style="width: 80%;">
            <input id="pw-email" class="swal2-input" placeholder="이메일" style="width: 80%;" value="${initialEmail}">
            <input id="pw-phone" class="swal2-input" placeholder="전화번호" style="width: 80%;" maxlength="13">
        `,
        showCancelButton: true,
        confirmButtonText: '인증번호 받기',
        cancelButtonText: '취소',
        confirmButtonColor: '#3085d6',
        cancelButtonColor: '#6e7881',
        didOpen: () => {
            const phoneInput = document.getElementById('pw-phone');
            if (phoneInput) phoneInput.oninput = (e) => { e.target.value = formatPhoneNumber(e.target.value); };
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

    if (info) await handlePasswordProcess(info.email, info, 'FIND_PW');
};

/**
 * 5. 비밀번호 변경/재설정 공통 프로세스
 * mode: 'FIND_PW' (기존) / 'VERIFY' (인증 전용)
 */
const handlePasswordProcess = async (email, info = null, mode = 'FIND_PW') => {
    try {
        Swal.fire({ title: '발송 중...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });
        await api.post("/api/user/auth/local/send-code", info || { email });

        let timerInterval;
        const { value: code } = await Swal.fire({
            title: '인증번호 입력',
            html: `
                <div style="margin-bottom: 10px;">이메일로 발송된 6자리 번호를 입력하세요.</div>
                <input id="swal-input-code" class="swal2-input" placeholder="인증번호" maxlength="6" style="width: 60%; text-align: center;">
                <div id="auth-timer" style="color: #e74c3c; font-weight: bold; margin-top: 10px; font-size: 1.1rem;">남은 시간: 03:00</div>
            `,
            showCancelButton: true,
            confirmButtonText: '인증하기',
            cancelButtonText: '취소',
            confirmButtonColor: '#3085d6',
            didOpen: () => {
                const timerDisplay = document.getElementById("auth-timer");
                let timeLeft = 180;
                timerInterval = setInterval(() => {
                    timeLeft -= 1;
                    const min = Math.floor(timeLeft / 60);
                    const sec = timeLeft % 60;
                    if (timerDisplay) timerDisplay.textContent = `남은 시간: ${String(min).padStart(2, '0')}:${String(sec).padStart(2, '0')}`;
                    if (timeLeft <= 0) {
                        clearInterval(timerInterval);
                        if (timerDisplay) timerDisplay.textContent = "시간 만료";
                        document.getElementById("swal-input-code").disabled = true;
                    }
                }, 1000);
            },
            willClose: () => clearInterval(timerInterval),
            preConfirm: () => {
                const val = document.getElementById("swal-input-code").value;
                if (!val || val.length < 6) {
                    Swal.showValidationMessage("인증번호 6자리를 입력해주세요.");
                    return false;
                }
                return val;
            }
        });

        if (!code) return;

        await api.post("/api/user/auth/local/verify-code", { email, code });

        // 새 비밀번호 설정 창 (본인 확인 완료 후 단계)
        const resetTitle = mode === 'VERIFY' ? '비밀번호 재설정' : '새 비밀번호 설정';
        
        const { value: pwValues } = await Swal.fire({
            title: resetTitle,
            html: `
                <div style="font-size: 0.85rem; color: #888; margin-bottom: 10px;">본인 확인이 완료되었습니다. 새 비밀번호를 설정해주세요.</div>
                <input type="password" id="new-pw" class="swal2-input" placeholder="새 비밀번호(8자 이상)" style="width: 80%;">
                <input type="password" id="confirm-pw" class="swal2-input" placeholder="비밀번호 확인" style="width: 80%;">
            `,
            showCancelButton: true,
            confirmButtonText: '변경하기',
            cancelButtonText: '취소',
            confirmButtonColor: '#3085d6',
            preConfirm: () => {
                const n = document.getElementById('new-pw').value;
                const c = document.getElementById('confirm-pw').value;
                if (n.length < 8) { Swal.showValidationMessage('8자 이상 입력해주세요.'); return false; }
                if (n !== c) { Swal.showValidationMessage('비밀번호가 일치하지 않습니다.'); return false; }
                return { newPassword: n, confirmPassword: c };
            }
        });

        if (pwValues) {
            await api.post("/api/user/auth/local/reset-password", { email, ...pwValues });
            Swal.fire('성공', mode === 'VERIFY' ? '본인 확인 및 비밀번호 변경이 완료되었습니다.' : '비밀번호가 변경되었습니다.', 'success');
        }
    } catch (error) {
        Swal.fire('에러', error.response?.data?.message || '처리에 실패했습니다.', 'error');
    }
};

/**
 * 6. 마이페이지용 비밀번호 변경
 */
export const handlePasswordChange = async (initialData = {}) => {
    const { name, email, phone } = initialData;
    const { value: info } = await Swal.fire({
        title: "비밀번호 변경",
        html: `
          <input id="pw-name" class="swal2-input" placeholder="이름" style="width:80%;" value="${name || ''}">
          <input id="pw-email" class="swal2-input" placeholder="이메일" style="width:80%;" value="${email || ''}">
          <input id="pw-phone" class="swal2-input" placeholder="전화번호" style="width:80%;" maxlength="13" value="${phone || ''}">
        `,
        showCancelButton: true,
        confirmButtonText: "인증번호 받기",
        cancelButtonText: '취소',
        confirmButtonColor: '#3085d6',
        didOpen: () => {
            const el = document.getElementById("pw-phone");
            if (el) el.oninput = (e) => { e.target.value = formatPhoneNumber(e.target.value); };
        },
        preConfirm: () => {
            const n = document.getElementById("pw-name").value;
            const em = document.getElementById("pw-email").value;
            const ph = document.getElementById("pw-phone").value;
            if (!n || !em || !ph) { Swal.showValidationMessage("모든 정보를 입력해주세요."); return false; }
            return { name: n, email: em, phone: ph };
        },
    });

    if (info) await handlePasswordProcess(info.email, info);
};

/**
 * 7. 로컬 계정 연동
 */
export const openLinkLocalPasswordModal = async (onSuccess) => {
    const { value: passwords } = await Swal.fire({
        title: '로컬 계정 연동',
        html: `
            <div style="text-align: left; width: 80%; margin: 0 auto; font-size: 14px; color: #666; margin-bottom: 10px;">
                연동 시 사용할 비밀번호를 설정해주세요.
            </div>
            <input type="password" id="link-pw" class="swal2-input" placeholder="새 비밀번호" style="width: 80%;">
            <input type="password" id="link-pw-confirm" class="swal2-input" placeholder="비밀번호 확인" style="width: 80%;">
        `,
        showCancelButton: true,
        confirmButtonText: '연동하기',
        cancelButtonText: '취소',
        confirmButtonColor: '#3085d6',
        preConfirm: () => {
            const p = document.getElementById('link-pw').value;
            const pc = document.getElementById('link-pw-confirm').value;
            if (!p || !pc) { Swal.showValidationMessage('비밀번호를 입력해주세요.'); return false; }
            if (p !== pc) { Swal.showValidationMessage('비밀번호가 일치하지 않습니다.'); return false; }
            return { password: p, passwordConfirm: pc };
        }
    });

    if (passwords) {
        try {
            await api.post("/api/user/auth/local/link-password", passwords);
            await Swal.fire('성공', '로컬 계정 연동이 완료되었습니다.', 'success');
            if (onSuccess) onSuccess();
        } catch (err) {
            Swal.fire('에러', err.response?.data?.message || '연동에 실패했습니다.', 'error');
        }
    }
};

/**
 * 8. 회원 탈퇴
 */
export const handleWithdraw = async (navigate) => {
    const { value: formValues } = await Swal.fire({
        title: '정말로 탈퇴하시겠습니까?',
        html: `
            <p style="color: #ff4d4f; font-size: 0.85rem; margin-bottom: 20px;">
                탈퇴 시 소셜 연동 정보가 삭제되며, 계정은 복구할 수 없습니다.
            </p>
            <input id="swal-input1" class="swal2-input" type="password" placeholder="비밀번호" style="width: 80%;">
            <input id="swal-input2" class="swal2-input" type="password" placeholder="비밀번호 확인" style="width: 80%;">
            <input id="swal-input3" class="swal2-input" placeholder="'회원 탈퇴'를 입력해주세요" style="width: 80%;">
        `,
        showCancelButton: true,
        confirmButtonText: '탈퇴하기',
        cancelButtonText: '취소',
        confirmButtonColor: '#d33',
        preConfirm: () => {
            const p = document.getElementById('swal-input1').value;
            const pc = document.getElementById('swal-input2').value;
            const t = document.getElementById('swal-input3').value;
            if (!p || !pc || !t) { Swal.showValidationMessage('모든 항목을 입력해주세요.'); return false; }
            if (p !== pc) { Swal.showValidationMessage('비밀번호가 일치하지 않습니다.'); return false; }
            if (t !== '회원 탈퇴') { Swal.showValidationMessage("'회원 탈퇴'를 정확히 입력해주세요."); return false; }
            return { password: p, confirmPassword: pc };
        }
    });

    if (formValues) {
        try {
            Swal.fire({ title: '처리 중...', didOpen: () => Swal.showLoading() });
            await api.delete('/api/user/auth/local/withdraw', { data: formValues });
            await Swal.fire('성공', '탈퇴 처리가 완료되었습니다.', 'success');
            localStorage.clear();
            navigate ? navigate('/') : window.location.href = '/';
        } catch (error) {
            Swal.fire('실패', error.response?.data?.message || '오류가 발생했습니다.', 'error');
        }
    }
};

/**
 * 9. 소셜 계정 복구 처리
 */
export const handleSocialRecover = async (data, onComplete) => {
    const result = await Swal.fire({
        title: '탈퇴 계정 복구 안내',
        html: `
            <div style="text-align: center;">
                <p><b>${data.email}</b> 계정은 현재 탈퇴 상태입니다.</p>
                <p>기존 정보를 복구하여 바로 로그인하시겠습니까?</p>
            </div>
        `,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: '계정 복구 후 로그인',
        cancelButtonText: '취소',
        confirmButtonColor: '#3085d6',
        footer: '<span style="color: #d33; font-size: 12px;">* 취소 시 해당 계정 가입이 제한될 수 있습니다.</span>',
        allowOutsideClick: false
    });

    if (result.isConfirmed) {
        Swal.fire({ title: '계정 복구 중...', didOpen: () => Swal.showLoading(), allowOutsideClick: false });
        try {
            const res = await api.post("/api/user/auth/social-recover", data);
            if (res.data.accessToken) {
                localStorage.setItem("accessToken", res.data.accessToken);
                localStorage.setItem("refreshToken", res.data.refreshToken);
                if (res.data.name) localStorage.setItem("userName", res.data.name);
                if (res.data.email) localStorage.setItem("userEmail", res.data.email);

                await Swal.fire({ icon: 'success', title: '복구 완료!', timer: 1500, showConfirmButton: false });
                window.location.href = "/dashboard";
            }
        } catch (error) {
            Swal.fire('실패', error.response?.data?.message || '복구 중 오류 발생', 'error');
            if (onComplete) onComplete();
        }
    } else {
        if (onComplete) onComplete();
    }
};