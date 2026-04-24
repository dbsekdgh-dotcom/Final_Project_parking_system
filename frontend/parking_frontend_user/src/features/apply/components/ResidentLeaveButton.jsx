import React from 'react';
import Swal from 'sweetalert2';
import { useLeaveResident } from '../hooks/useApply';
import '../../mypage/components/UserStatusCard.css';

const ResidentLeaveButton = () => {
    const { mutate: leave, isPending } = useLeaveResident();

    const handleLeave = () => {
        Swal.fire({
            title: '퇴거 신청',
            text: '퇴거 처리 후에는 입주민 서비스를 이용할 수 없습니다. 진행하시겠습니까?',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#d33',
            cancelButtonColor: '#6c757d',
            confirmButtonText: '퇴거',
            cancelButtonText: '취소',
        }).then((result) => {
            if (result.isConfirmed) leave();
        });
    };

    return (
        <button
            className="btn-cancel"
            onClick={handleLeave}
            disabled={isPending}
        >
            {isPending ? '처리 중...' : '퇴거'}
        </button>
    );
};

export default ResidentLeaveButton;
