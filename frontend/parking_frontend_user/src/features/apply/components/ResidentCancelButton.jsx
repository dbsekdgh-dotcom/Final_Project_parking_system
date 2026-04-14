import React from 'react';
import Swal from 'sweetalert2';
import { useCancelApply } from '../hooks/useApply';
import '../../mypage/components/UserStatusCard.css';

const ResidentCancelButton = ({ approvalId }) => {
    const { mutate: cancel, isPending } = useCancelApply();

    const handleCancel = () => {
        Swal.fire({
            title: '신청을 취소하시겠습니까?',
            text: '취소 후에는 다른 호수를 다시 선택해야 합니다.',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#d33',
            cancelButtonColor: '#6c757d',
            confirmButtonText: '확인',
            cancelButtonText: '취소',
        }).then((result) => {
            if (result.isConfirmed) cancel(approvalId);
        });
    };

    return (
        <button
            className="btn-cancel"
            onClick={handleCancel}
            disabled={isPending}
        >
            {isPending ? '처리 중...' : '신청 취소'}
        </button>
    );
};

export default ResidentCancelButton;
