import adminApi from '../../../../shared/api/adminApi';

export const getApprovals = (params) =>
  adminApi.get('/approvals', { params }).then((r) => r.data);

export const getApprovalPendingCount = () =>
  adminApi.get('/approvals', { params: { page: 0, size: 1 } })
    .then((r) => r.data.stats?.pendingCount ?? 0);

export const approveApproval = (approvalId) =>
  adminApi.post(`/approvals/${approvalId}/approve`);

export const rejectApproval = (approvalId, rejectReason) =>
  adminApi.post(`/approvals/${approvalId}/reject`, { rejectReason });
