const BASE = '/api/admin/management';

const get = (url) =>
  fetch(url, {
    credentials: 'include',
    headers: { Authorization: `Bearer ${localStorage.getItem('accessToken')}` },
  }).then((r) => r.json());

const buildParams = (obj) => {
  const p = new URLSearchParams();
  Object.entries(obj).forEach(([k, v]) => {
    if (v !== '' && v !== null && v !== undefined) p.append(k, v);
  });
  return p.toString();
};

export const fetchUsers              = (p)  => get(`${BASE}/users?${buildParams(p)}`);
export const fetchUserDetail         = (id) => get(`${BASE}/users/${id}`);

export const fetchVehicles           = (p)  => get(`${BASE}/vehicles?${buildParams(p)}`);
export const fetchVehicleDetail      = (id) => get(`${BASE}/vehicles/${id}`);

export const fetchSubscriptions      = (p)  => get(`${BASE}/subscriptions?${buildParams(p)}`);
export const fetchSubscriptionDetail = (id) => get(`${BASE}/subscriptions/${id}`);

export const fetchReservations       = (p)  => get(`${BASE}/reservations?${buildParams(p)}`);
export const fetchReservationDetail  = (id) => get(`${BASE}/reservations/${id}`);
