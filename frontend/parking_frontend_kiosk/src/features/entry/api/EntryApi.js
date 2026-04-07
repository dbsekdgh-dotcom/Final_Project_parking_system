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
