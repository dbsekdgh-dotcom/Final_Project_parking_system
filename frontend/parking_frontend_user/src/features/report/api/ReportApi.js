import api from "../../auth/api/axios";

const BASE = "/api/report";

// 내가 신고한 내역 (CANCELLED 제외)
export const fetchMyReports = async (page = 0, size = 5) => {
  const res = await api.get(`${BASE}/my`, { params: { page, size } });
  return res.data; // Page 객체 { content, totalPages, totalElements, ... }
};

// 내가 받은 신고 (CANCELLED 제외)
export const fetchReceivedReports = async (page = 0, size = 5) => {
  const res = await api.get(`${BASE}/received`, { params: { page, size } });
  return res.data;
};

// 신고 취소
export const cancelReport = async (reportId) => {
  const res = await api.patch(`${BASE}/${reportId}/cancel`);
  return res.data;
};
