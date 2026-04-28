import kioskApi from "../../../shared/api/kioskApi";

const BASE = "/api/v1/entry";


export const createEntry = async ({ plateNumber, s3path, cameraId }) => {
  const res = await kioskApi.post(BASE,null,{
    params:{ plateNumber,s3path,cameraId }
  });
  return res.data; 
};

// 차번호로 현재 ENTERED 상태인지 조회 → { isEntered, parkingLogId }
export const checkVehicleEntered = async (carNumber) => {
  const res = await kioskApi.get(`${BASE}/check`, { params: { carNumber } });
  return res.data;
};

// ENTRY 카메라 목록 조회
export const fetchEntryCameras = async () => {
  const res = await kioskApi.get(`${BASE}/cameras`);
  return res.data;
};

// EXIT 카메라 목록 조회
export const fetchExitCameras = async () => {
  const res = await kioskApi.get(`${BASE}/cameras/exit`);
  return res.data;
};

// 자리 선택 + 입차 확정: DETECTED → ENTERED (하나의 트랜잭션)
export const confirmEnter = async ({ parkingLogId, spaceId }) => {
  const res = await kioskApi.patch(`${BASE}/${parkingLogId}/enter`, null, {
    params: { spaceId },
  });
  return res.data;
};

// parkingLog의 타입(RESIDENT/VISIT/USER/RESERVATION) 조회
export const fetchParkingLogType = async (parkingLogId) => {
  const res = await kioskApi.get(`${BASE}/${parkingLogId}/type`);
  return res.data; // { parkingTypeSnapshot }
};

// 층별 주차 공간 목록 조회
export const fetchEntrySpace = async (floor) => {
  const res = await kioskApi.get(`${BASE}/space`, { params: { floor } });
  return res.data;
};

// DETECTED → ENTRY_CANCELLED (회차 버튼)
export const cancelEntry = async (parkingLogId) => {
  const res = await kioskApi.patch(`${BASE}/${parkingLogId}/cancel`);
  return res.data;
};
