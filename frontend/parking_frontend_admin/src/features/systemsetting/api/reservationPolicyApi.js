import adminApi from "../../../shared/api/adminApi"

export const fetchActivePolicyApi = () =>
    adminApi.get("/reservation-policy/active");

export const fetchPoliciesApi = (page = 0, size = 10) =>
    adminApi.get("/reservation-policy", { params: { page, size } });

export const createPolicyApi = (dto) =>
    adminApi.post("/reservation-policy", dto);

export const updatePolicyApi = (policyId, dto) =>
    adminApi.put(`/reservation-policy/${policyId}`, dto);

