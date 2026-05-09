import kioskApi from "./kioskApi";


//keypay로 입력한 차량번호 네글자로 차량 리스트 조회
export const searchCar=async(searchKeyword)=>{
    const res=await kioskApi.post(`/api/payment/search-car`,{vehicleNumber:searchKeyword})
    return res.data;
}

//사용자가 선택한 차량번호로 결제 정보 조회
export const requestPayment=async(selectedVehicle)=>{
    const res=await kioskApi.post(`/api/payment/request-payment`,selectedVehicle)
    console.log("차량 결제 정보==>"+res.data);  
    return res.data;
}

//결제정보로 결제 요청->백엔드에 확인 요청 
export const requestBeforePayment=async(settlementPayload)=>{
    const res=await kioskApi.post(`/api/payment/request-ready-payment`,settlementPayload)
    console.log("res==>"+res.data);
    return res.data;
}
//결제 취소 (뒤로가기 시 락 해제)
export const cancelPayment=async(carNumber)=>{
    await kioskApi.post(`/api/payment/cancel`,{carNumber})
}

//결제 완료된 경우
export const requestAfterPayment=async(settlementConfirmPayload)=>{
    const res=await kioskApi.post(`/api/payment/request-after-payment`,settlementConfirmPayload)
    console.log("결제 후 후속처리 요청 정보==>"+res.data)
    return res.data;
}
