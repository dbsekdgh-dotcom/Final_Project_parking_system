import axios from "axios";

const BASE = "http://localhost:8081/api/v1/entry";

// 입차 감지: 파일 전송 → parkingLogId 반환
export const createEntry = async ({ file }) => {
  const formData = new FormData();
  formData.append("file", file);

  const res = await axios.post(BASE, formData);
  return res.data; // { parkingLogId }
};

// ENTRY 카메라 목록 조회
export const fetchEntryCameras = async () => {
  const res = await axios.get(`${BASE}/cameras`);
  return res.data;
};

// 입구 카메라 선택 → 입차 확정
export const confirmEnter = async ({ parkingLogId, cameraId }) => {
  const res = await axios.patch(`${BASE}/${parkingLogId}/enter`, null, {
    params: { cameraId },
  });
  return res.data;
};

// parkingLog의 타입(RESIDENT/VISIT/USER/RESERVATION) 조회
export const fetchParkingLogType = async (parkingLogId) => {
  const res = await axios.get(`${BASE}/${parkingLogId}/type`);
  return res.data; // { parkingTypeSnapshot }
};

// 층별 주차 공간 목록 조회
export const fetchEntrySpace = async (floor) => {
  const res = await axios.get(`${BASE}/space`, { params: { floor } });
  return res.data;
};

// 자리 선택 → parking_log 업데이트
export const assignParkingSpace = async ({ parkingLogId, spaceId }) => {
  const res = await axios.patch(`${BASE}/${parkingLogId}/space`, null, {
    params: { spaceId },
  });
  return res.data;
};
