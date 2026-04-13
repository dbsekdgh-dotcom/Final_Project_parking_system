import React from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import Swal from 'sweetalert2';
import { cancelResidentApply } from '../api/mypageApi';
import './UserStatusCard.css';

const ResidentCancelButton = ({ approvalId }) => {
    const queryClient = useQueryClient();

    const cancelMutation = useMutation({
        mutationFn: () => cancelResidentApply(approvalId),
        onSuccess: () => {
            // 💡 "PENDING" 상태가 "NONE"으로 바뀌었으니 다시 그려라!
            queryClient.invalidateQueries({ queryKey: ["userStatus"] });
            queryClient.invalidateQueries({ queryKey: ["unitStatus"] });

            Swal.fire({
                icon: "success",
                title: "취소 완료",
                text: "입주 신청이 정상적으로 취소되었습니다.",
                confirmButtonColor: "#3085d6",
            });
        },
        onError: (error) => {
            Swal.fire({
                icon: "error",
                title: "취소 실패",
                text: error.response?.data?.message || "취소 중 오류가 발생했습니다.",
            });
        },   
    });

    const handleCancel = () => {
        Swal.fire({
            title: "신청을 취소하시겠습니까?",
            text: "취소 후에는 다른 호수를 다시 선택해야 합니다.",
            icon: "warning",
            showCancelButton: true,
            confirmButtonColor: "#d33",
            cancelButtonColor: "#6c757d",
            confirmButtonText: "확인",
            cancelButtonText: "취소",
        }).then((result) => {
            if (result.isConfirmed) {
                cancelMutation.mutate();
            }
        });
    };

    return (
        <button
            className="btn-cancel"
            onClick={handleCancel}
            disabled={cancelMutation.isPending}
        >
            {cancelMutation.isPending ? "처리 중..." : "신청 취소"}
        </button>
    );
};

export default ResidentCancelButton;