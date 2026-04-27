import adminApi from '../../../../shared/api/adminApi';

export const getReports = (params) =>
  adminApi.get('/reports', { params }).then((r) => r.data);

export const getReportPendingCount = () =>
  adminApi.get('/reports', { params: { page: 0, size: 1 } })
    .then((r) => r.data.stats?.pendingCount ?? 0);

export const approveReport = (reportId) =>
  adminApi.post(`/reports/${reportId}/approve`);

export const rejectReport = (reportId, rejectReason) =>
  adminApi.post(`/reports/${reportId}/reject`, { rejectReason });
