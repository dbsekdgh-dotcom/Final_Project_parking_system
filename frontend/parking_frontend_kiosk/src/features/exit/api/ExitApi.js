import kioskApi from "../../../shared/api/kioskApi";

const BASE = "/api/exit";

//출차 요청 : ENTERED -> EXIT_REQUESTED
// data 반환 값 : VehiclePaymentResponseDto
export const requestExit = async (parkingLogId, exitCameraId, imagePath) =>{
    const res = await kioskApi.post(`${BASE}/request`,null,{
        params: { parkingLogId, exitCameraId, imagePath}
    });
    return res.data
};

// 출차 확정
export const confirmExit = async (parkingLogId) =>{
    await kioskApi.post(`${BASE}/confirm`,null,{
        params: { parkingLogId }
    });
};

// 회차 : EXIT_REQUESTED -> ENTERED
export const cancelExit = async (parkingLogId) =>{
    await kioskApi.patch(`${BASE}/cancel`,null,{
        params: { parkingLogId }
    });
};