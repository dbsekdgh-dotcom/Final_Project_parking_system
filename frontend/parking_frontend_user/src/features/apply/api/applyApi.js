import api from '../../auth/api/axios';

export const fetchUnitStatus = () =>
    api.get('/api/user/apply/unit-status').then(r => r.data);

export const fetchHouseholds = () =>
    api.get('/api/user/apply/households').then(r => r.data);

export const applyResident = (householdId) =>
    api.post('/api/user/apply/resident', { householdId }).then(r => r.data);

// 유저의 현재 입주 신청 상태 및 activeApprovalId 조회
export const fetchUserStatus = () =>
    api.get('/api/user/apply/status').then(r => r.data);

// 입주민 신청 취소
export const cancelResidentApply = (approvalId) =>
    api.patch(`/api/user/apply/resident/${approvalId}/cancel`).then(r => r.data);

// 퇴거
export const leaveResident = () =>
    api.delete('/api/user/apply/resident').then(r => r.data);
