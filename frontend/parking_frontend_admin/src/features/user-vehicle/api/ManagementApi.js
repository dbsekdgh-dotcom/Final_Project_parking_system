import adminApi from '../../../shared/api/adminApi';

const BASE = '/management';

const clean = (obj) =>
  Object.fromEntries(
    Object.entries(obj).filter(([, v]) => v !== '' && v !== null && v !== undefined)
  );

const get = (url, params) =>
  adminApi.get(url, { params: clean(params ?? {}) }).then((r) => r.data);

export const fetchUsers              = (p)  => get(`${BASE}/users`, p);
export const fetchUserDetail         = (id) => get(`${BASE}/users/${id}`);

export const fetchVehicles           = (p)  => get(`${BASE}/vehicles`, p);
export const fetchVehicleDetail      = (id) => get(`${BASE}/vehicles/${id}`);

export const fetchSubscriptions      = (p)  => get(`${BASE}/subscriptions`, p);
export const fetchSubscriptionDetail = (id) => get(`${BASE}/subscriptions/${id}`);

export const fetchReservations       = (p)  => get(`${BASE}/reservations`, p);
export const fetchReservationDetail  = (id) => get(`${BASE}/reservations/${id}`);
