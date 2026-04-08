import Swal from "sweetalert2";
import { AUTH_ERROR_CODES } from "../constants/errorCodes";



export const handleAuthError = (error, navigate, email, actions) => {
    const errorCode = error.response?.data?.code;
    const serverMessage = error.response?.data?.message || "오류가 발생했습니다.";

    const showAlert = (icon, title, text, customConfig = {}) => {
        return Swal.fire({
            icon,
            title,
            text,
            confirmButtonColor: '#3085d6',
            ...customConfig
        });
    };

    switch (errorCode) {
        case AUTH_ERROR_CODES.TOO_MANY_ATTEMPTS:
            showAlert('warning', '로그인 차단 알림', '비밀번호를 5회 이상 틀려 로그인이 차단되었습니다.', {
                html: `
                    <div style="text-align: center;">
                        <p style="font-weight: bold; color: #e74c3c; font-size: 18px; margin-bottom: 10px;">
                            비밀번호를 5회 이상 틀려 로그인이 차단되었습니다.
                        </p>
                        <p style="font-size: 15px; color: #333; line-height: 1.6;">
                            현재 이 계정은 보안을 위해 차단된 상태입니다.<br>
                            <b>5분 뒤에 다시 시도</b>하거나,<br>
                            지금 바로 <b>이메일 인증</b>을 통해 차단을 해제하세요.
                        </p>
                    </div>
                `,
                showCancelButton: true,
                confirmButtonText: '이메일 인증하기',
                cancelButtonText: '취소',
                cancelButtonColor: '#6c757d'
            }).then((result) => {
                if (result.isConfirmed && actions?.onVerify) {
                    actions.onVerify(email);
                }
            });
            break;

        case AUTH_ERROR_CODES.WITHDRAWN_ACCOUNT:
            showAlert('error', '탈퇴된 계정입니다.', serverMessage, {
                showCancelButton: true,
                confirmButtonText: '계정 복구',
                cancelButtonText: '취소'
            }).then((result) => {
                if (result.isConfirmed && actions?.onRecover) {
                    actions.onRecover(email);
                }
            });
            break;

        case AUTH_ERROR_CODES.USER_NOT_FOUND:
            showAlert('question', '계정을 찾을 수 없습니다', '회원가입 페이지로 이동할까요?', {
                showCancelButton: true,
                confirmButtonText: '이동하기',
                cancelButtonText: '취소',
            }).then((result) => {
                if (result.isConfirmed) navigate("/signup");
            });
            break;

        case AUTH_ERROR_CODES.SOCIAL_USER_LOGIN_ATTEMPT:
            showAlert('info', '이미 가입된 계정입니다', '해당 계정은 소셜로그인으로 가입되었습니다. 카카오/네이버 로그인 버튼을 이용해 주세요.');
            break;

        default:
            showAlert('error', '로그인 실패', serverMessage);
            break;
    }
}

