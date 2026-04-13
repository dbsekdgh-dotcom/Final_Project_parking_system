import api from '../../auth/api/axios';

// ── 마이페이지 대시보드 ──────────────────────────────

// JWT 토큰에서 userId 추출 (Principal) → URL에 userId 불필요
export const fetchMyInfo = () =>
    api.get('/api/user/mypage').then(r => r.data);

// phone 또는 birth 중 변경할 필드만 담아서 호출
export const updateMyInfo = (data) =>
    api.put('/api/user/mypage', data).then(r => r.data);

// ── 입주민 신청 ────────────────────────────────────

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