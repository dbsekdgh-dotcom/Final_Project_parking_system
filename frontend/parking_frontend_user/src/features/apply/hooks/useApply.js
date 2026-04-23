import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import Swal from 'sweetalert2';
import { fetchUnitStatus, applyResident, cancelResidentApply } from '../api/applyApi';

export const useUnitStatus = () => {
    return useQuery({
        queryKey: ['unitStatus'],
        queryFn: fetchUnitStatus,
        select: (data) => (Array.isArray(data) ? data : []),
    });
};

export const useApplyResident = (onSuccess) => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (householdId) => applyResident(householdId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['userStatus'] });
            queryClient.invalidateQueries({ queryKey: ['unitStatus'] });
            queryClient.invalidateQueries({ queryKey: ['myInfo'] });
            onSuccess?.();
            Swal.fire({
                icon: 'success',
                title: '신청 완료',
                html: `
                    <p>입주민 등록 신청이 완료되었습니다.</p>
                    <p style="color:#888; font-size:0.88rem; margin-top:8px;">관리자 승인 후 서비스를 이용하실 수 있습니다.</p>
                `,
                confirmButtonColor: '#3085d6',
            });
        },
        onError: (err) => {
            Swal.fire({
                icon: 'error',
                title: '신청 실패',
                text: err.response?.data?.message || '신청 중 오류가 발생했습니다.',
                confirmButtonColor: '#d33',
            });
        },
    });
};

export const useCancelApply = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (approvalId) => cancelResidentApply(approvalId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['userStatus'] });
            queryClient.invalidateQueries({ queryKey: ['unitStatus'] });
            Swal.fire({
                icon: 'success',
                title: '취소 완료',
                text: '입주 신청이 정상적으로 취소되었습니다.',
                confirmButtonColor: '#3085d6',
            });
        },
        onError: (error) => {
            Swal.fire({
                icon: 'error',
                title: '취소 실패',
                text: error.response?.data?.message || '취소 중 오류가 발생했습니다.',
            });
        },
    });
};
