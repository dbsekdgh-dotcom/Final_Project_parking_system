import axios from "axios";

const BASE = "http://localhost:8081/api/exit";

//출차 요청 : ENTERED -> EXIT_REQUESTED
// data 반환 값 : VehiclePaymentResponseDto
export const requestExit = async (parkingLogId, exitCameraId, imagePath) =>{
    const res = await axios.post(`${BASE}/request`,null,{
        params: { parkingLogId, exitCameraId, imagePath}
    });
    return res.data
};

// 출차 확정
export const confirmExit = async (parkingLogId) =>{
    await axios.post(`${BASE}/confirm`,null,{
        params: { parkingLogId }
    });
};

// 회차 : EXIT_REQUESTED -> ENTERED
export const cancelExit = async (parkingLogId) =>{
    await axios.patch(`${BASE}/confirm`,null,{
        params: { parkingLogId }
    });
};