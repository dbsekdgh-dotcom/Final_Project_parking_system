import React from 'react'
import { useNavigate } from 'react-router-dom'

const WithdrawButton = () => {

    const navigate = useNavigate();

    const handleWithdraw = async () => {
        const { value: formValues } = await Swal.fire({
            title: '정말로 탈퇴하시겠습니까?',
            html:
            '<p style="color: #ff4d4f; font-size: 0.9rem; margin-bottom: 20px;">' +
            '탈퇴 시 소셜 연동 정보가 삭제되며, 계정은 복구할 수 없습니다.' +
            '</p>' +
            '<input id="swal-input1" class="swal2-input" type="password" placeholder="비밀번호">' +
                '<input id="swal-input2" class="swal2-input" type="password" placeholder="비밀번호 확인">' +
                '<input id="swal-input3" class="swal2-input" placeholder="\'회원 탈퇴\'를 입력해주세요">',
            focusConfirm: false,
            showCancelButton: true,
            confirmButtonText: '탈퇴하기',
            cancelButtonText: '취소',
            confirmButtonColor: '#d33',
            preConfirm: () => {
                const password = document.getElementById('swal-input1').value;
                const confirmPassword = document.getElementById('swal-input2').value;
                const confirmText = document.getElementById('swql-input3').value;

                if (!password || !confirmPassword || !confirmText) {
                    Swal.showValidationMessage('모든 항목을 입력해주세요.');
                }
                return { password, confirmPassword, confirmText };
            }
        });

        if (formValues) {
            
        }
    }


    return (
        <div>WithdrawButton</div>
    )
}

export default WithdrawButton