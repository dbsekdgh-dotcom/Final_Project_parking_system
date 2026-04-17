import Swal from 'sweetalert2';
import api from '../api/axios';

/**
 * 1. 휴대폰 번호 포맷팅 도구
 */
export const formatPhoneNumber = (value) => {
    if (!value) return value;
    const phoneNumber = value.replace(/[^\d]/g, '');
    const cpLen = phoneNumber.length;
    if (cpLen < 4) return phoneNumber;
    if (cpLen < 8) return `${phoneNumber.slice(0, 3)}-${phoneNumber.slice(3)}`;
    return `${phoneNumber.slice(0, 3)}-${phoneNumber.slice(3, 7)}-${phoneNumber.slice(7, 11)}`;
};

/**
 * 2. 로컬 계정 복구 (탈퇴 계정 통합 모달)
 * - 첫 번째 이미지 디자인 적용: icon: 'warning', confirmButtonColor: '#e67e22'
 */
export const handleLocalRecover = async (email, prefilled = {}) => {
    // Step 1: 탈퇴 계정 안내 (원하는 첫 번째 이미지 UI)
    const { isConfirmed } = await Swal.fire({
        icon: 'warning', // 노란색 느낌표 아이콘
        title: '탈퇴한 계정입니다',
        html: `
            <div style="margin: 16px 0;">
                <p style="color: #666; margin-bottom: 8px;">현재 <b>탈퇴 상태</b>인 계정입니다.</p>
                <b style="font-size: 1.2rem; color: #d33;">${email}</b>
                <p style="color: #888; font-size: 0.9rem; margin-top: 10px;">복구하시겠습니까?</p>
            </div>
        `,
        showCancelButton: true,
        confirmButtonText: '계정복구하기',
        cancelButtonText: '취소',
        confirmButtonColor: '#e67e22', // 주황색 버튼
        cancelButtonColor: '#6e7881',
    });

    if (!isConfirmed) return;

    // Step 2: 본인 확인 입력
    const { value: info } = await Swal.fire({
        title: '본인 확인',
        html: `
            <div style="text-align: left; width: 80%; margin: 0 auto; font-size: 13px; color: #888; margin-bottom: 10px;">
                계정 복구를 위해 가입 정보를 입력해주세요.
            </div>
            <input id="recover-name" class="swal2-input" placeholder="이름" style="width: 80%;" value="${prefilled.name || ''}">
            <input id="recover-email" class="swal2-input" placeholder="이메일" style="width: 80%;">
            <input id="recover-phone" class="swal2-input" placeholder="전화번호" style="width: 80%;" maxlength="13" value="${prefilled.phone || ''}">
        `,
        showCancelButton: true,
        confirmButtonText: '인증번호 받기',
        cancelButtonText: '취소',
        confirmButtonColor: '#3085d6',
        didOpen: () => {
            const phoneInput = document.getElementById('recover-phone');
            if (phoneInput) phoneInput.oninput = (e) => { e.target.value = formatPhoneNumber(e.target.value); };
        },
        preConfirm: () => {
            const name = document.getElementById('recover-name').value;
            const recoverEmail = document.getElementById('recover-email').value;
            const phone = document.getElementById('recover-phone').value;
            if (!name || !recoverEmail || !phone) {
                Swal.showValidationMessage('모든 정보를 입력해주세요.');
                return false;
            }
            return { name, email: recoverEmail, phone };
        }
    });

    if (info) await handlePasswordProcess(info.email, info, 'RECOVER');
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
                await handleLocalRecover(email, { name: formValues.name, phone: formValues.phone });
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
                    const loginBtn = document.getElementById('go-login');
                    const pwBtn = document.getElementById('go-find-pw');
                    if(loginBtn) loginBtn.onclick = () => Swal.close();
                    if(pwBtn) pwBtn.onclick = () => { Swal.close(); openFindPwModal(email); };
                }, 100);
            }
        } catch (error) {
            Swal.fire('실패', error.response?.data?.message || '일치하는 정보가 없습니다.', 'error');
        }
    }
};

/**
 * 4-1. 이메일 인증 전용 모달 (계정 잠금 해제/본인 확인용)
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
 * 4-2. 비밀번호 찾기 모달
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
 * 5. 비밀번호 변경/재설정/복구 공통 프로세스
 */
const handlePasswordProcess = async (email, info = null, mode = 'FIND_PW') => {
    // code를 try 블록 바깥에서 선언해야 5-4 preConfirm 클로저에서 접근 가능
    let code;
    try {
        // 5-1. 인증번호 발송
        Swal.fire({ title: '발송 중...', allowOutsideClick: false, showConfirmButton: false, willOpen: () => Swal.showLoading() });
        const sendEndpoint = mode === 'RECOVER'
            ? "/api/user/auth/local/send-recover-code"
            : "/api/user/auth/local/send-code";
        await api.post(sendEndpoint, info || { email });

        // 5-2. 인증번호 입력 모달 (타이머 포함)
        let timerInterval;
        const { value: inputCode } = await Swal.fire({
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

        code = inputCode;
        if (!code) return;

        // 5-3. 인증코드 검증 (RECOVER 모드는 서버 통합 처리이므로 제외)
        if (mode !== 'RECOVER') {
            await api.post("/api/user/auth/local/verify-code", { email, code });
        }
    } catch (error) {
        Swal.hideLoading();
        Swal.close();
        await Swal.fire({
            icon: 'error',
            title: '에러',
            text: error.response?.data?.message || '처리에 실패했습니다.',
            confirmButtonText: '확인',
            confirmButtonColor: '#d33'
        });
        return;
    }

    // 5-4 & 5-5. 비밀번호 입력 + 서버 전송 (preConfirm 내부에서 API 호출 — 별도 로딩 다이얼로그 없음)
    const resetTitle = mode === 'VERIFY' ? '비밀번호 재설정'
        : mode === 'RECOVER' ? '계정 복구 비밀번호 설정'
        : '새 비밀번호 설정';

    const { value: pwValues } = await Swal.fire({
        title: resetTitle,
        html: `
            <div style="font-size: 0.85rem; color: #888; margin-bottom: 10px;">본인 확인이 완료되었습니다. 새 비밀번호를 설정해주세요.</div>
            <input type="password" id="new-pw" class="swal2-input" placeholder="새 비밀번호(8자 이상)" style="width: 80%;">
            <input type="password" id="confirm-pw" class="swal2-input" placeholder="비밀번호 확인" style="width: 80%;">
        `,
        showCancelButton: true,
        confirmButtonText: mode === 'RECOVER' ? '복구 완료' : '변경하기',
        cancelButtonText: '취소',
        confirmButtonColor: '#3085d6',
        showLoaderOnConfirm: true,
        allowOutsideClick: () => !Swal.isLoading(),
        preConfirm: async () => {
            const n = document.getElementById('new-pw').value;
            const c = document.getElementById('confirm-pw').value;

            // 클라이언트 검증 — 실패 시 모달 유지
            if (n.length < 8) { Swal.showValidationMessage('8자 이상 입력해주세요.'); return false; }
            if (n !== c) { Swal.showValidationMessage('비밀번호가 일치하지 않습니다.'); return false; }

            // 백엔드 DTO 필드명이 모드별로 다름
            // RECOVER → UserRecoverRequestDto.passwordConfirm
            // 그 외   → UserPasswordResetRequestDto.confirmPassword
            const pwData = mode === 'RECOVER'
                ? { newPassword: n, passwordConfirm: c }
                : { newPassword: n, confirmPassword: c };

            try {
                // 5-5. 서버 전송
                if (mode === 'RECOVER') {
                    await api.post("/api/user/auth/local/recover", { email, authCode: code, ...pwData });
                } else {
                    await api.post("/api/user/auth/local/reset-password", { email, ...pwData });
                }
                return pwData; // 성공 → 모달 닫힘
            } catch (error) {
                const msg = error.response?.data?.message || '처리에 실패했습니다.';
                const errorCode = error.response?.data?.code;
                // 재시도 불가 에러: 인증번호 만료/불일치, 세션 만료, 복구 불가 상황
                const NON_RETRYABLE = [
                    'INVALID_AUTH_CODE',       // 인증번호 만료 or 불일치 → 처음부터 다시
                    'VERIFICATION_CODE_EXPIRED', // 인증번호 유효시간 초과
                    'UNAUTHORIZED_ACCESS',      // 이메일 인증 안 된 접근
                    'PHONE_ALREADY_ACTIVE',     // 이미 활성 계정이 동일 번호 사용 중
                ];
                const shouldClose = error.response?.status === 401
                    || error.response?.status === 404
                    || NON_RETRYABLE.includes(errorCode)
                    || msg.includes('만료') || msg.includes('expired') || msg.includes('초과');
                Swal.showValidationMessage(msg);
                // shouldClose면 undefined 반환(모달 닫힘), 그 외는 false(모달 유지 후 재시도)
                return shouldClose ? undefined : false;
            }
        }
    });

    // 5-6. 성공 후처리
    if (pwValues) {
        if (mode === 'RECOVER') {
            await Swal.fire({
                icon: 'success',
                title: '성공',
                text: '계정이 복구되었습니다. 다시 로그인해주세요.',
                confirmButtonText: '확인',
                confirmButtonColor: '#3085d6'
            });
            if (window.location.pathname !== '/') window.location.href = '/';
        } else {
            await Swal.fire({
                icon: 'success',
                title: '성공',
                text: '비밀번호가 성공적으로 변경되었습니다.',
                confirmButtonText: '확인',
                confirmButtonColor: '#3085d6'
            });
        }
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
            return { password: p, confirmPassword: pc, confirmText: t};
        }
    });

    if (formValues) {
        try {
            Swal.fire({ title: '처리 중...', didOpen: () => Swal.showLoading() });
            await api.delete('/api/user/auth/local/withdraw', { data: formValues });
            await Swal.fire({
                icon: 'success',
                title: '탈퇴 완료',
                html: `
                    <p>탈퇴 처리가 완료되었습니다.</p>
                    <p style="color: #888; font-size: 0.9rem; margin-top: 8px;">그동안 이용해주셔서 감사합니다.</p>
                `,
                confirmButtonText: '확인',
                confirmButtonColor: '#3085d6',
            });
            localStorage.clear();
            sessionStorage.clear(); // AuthRoute가 sessionStorage.sessionActive로 인증 판단 → 반드시 제거
            // 탈퇴 후 서버에 로그아웃 요청 → HttpOnly 쿠키(accessToken/refreshToken) 만료 처리
            try { await api.post('/api/user/auth/local/logout'); } catch (_) { /* 이미 탈퇴된 계정이므로 무시 */ }
            window.location.replace('/');
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
            // 토큰은 HttpOnly 쿠키로 자동 저장, UI 데이터만 localStorage에 저장
            localStorage.setItem("userName", res.data.name || "");
            localStorage.setItem("userEmail", res.data.email || "");
            sessionStorage.setItem("sessionActive", "true");

            await Swal.fire({ icon: 'success', title: '복구 완료!', timer: 1500, showConfirmButton: false });
            window.location.href = "/dashboard";
        } catch (error) {
            Swal.fire('실패', error.response?.data?.message || '복구 중 오류 발생', 'error');
            if (onComplete) onComplete();
        }
    } else {
        if (onComplete) onComplete();
    }
};

/**
 * 10. 이메일 중복 확인 및 탈퇴 계정 체크
 * - 핵심: WITHDRAWN_ACCOUNT 에러 시 주황색 복구 모달(handleLocalRecover)을 즉시 호출
 */
export const checkEmailAvailability = async (email) => {
    if (!email || !email.trim()) {
        Swal.fire({ icon: 'warning', title: '입력 오류', text: '이메일을 입력해주세요.', confirmButtonText: '확인', confirmButtonColor: '#3085d6' });
        return false;
    }

    try {
        await api.post('/api/user/auth/local/check-email', { email });
        const { isConfirmed } = await Swal.fire({
            icon: 'success',
            title: '사용 가능한 이메일입니다',
            text: '이 이메일로 가입을 진행하시겠습니까?',
            showCancelButton: true,
            confirmButtonText: '사용하기',
            cancelButtonText: '취소',
            confirmButtonColor: '#3085d6',
            cancelButtonColor: '#6e7881',
        });
        return isConfirmed;
    } catch (error) {
        // 1. 에러 응답 정보 추출
        const status = error.response?.status;
        const errorCode = error.response?.data?.code; // UserErrorResponse의 code 필드

        // 2. 409 Conflict이고 탈퇴 계정인 경우
        if (status === 409 && errorCode === 'WITHDRAWN_ACCOUNT') {
            // 중복된 X 모달이 뜨지 않도록 혹시 모를 기존 창 닫기
            if (Swal.isVisible()) Swal.close();
            
            // 우리가 만든 주황색 복구 모달 실행
            handleLocalRecover(email); 
            return false;
        }

        // 3. 일반 중복(EMAIL_DUPLICATE) 등 기타 에러 처리
        const errorMsg = error.response?.data?.message || '이미 사용 중인 이메일입니다.';
        Swal.fire('알림', errorMsg, 'error');
        return false;
    }
};

/**
 * [CRITICAL] LoginPage.jsx 등 기존 코드에서 참조하는 이름을 위한 별칭
 */
