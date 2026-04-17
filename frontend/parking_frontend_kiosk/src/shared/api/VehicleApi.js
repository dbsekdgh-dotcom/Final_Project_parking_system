import axios from "axios"

export const host=(import.meta.env.VITE_API_BASE_URL || '')+'/api/payment'

//keypay로 입력한 차량번호 네글자로 차량 리스트 조회
export const searchCar=async(searchKeyword)=>{
    const res=await axios.post(`${host}/search-car`,{vehicleNumber:searchKeyword})
    return res.data;
}

//사용자가 선택한 차량번호로 결제 정보 조회
export const requestPayment=async(selectedVehicle)=>{
    const res=await axios.post(`${host}/request-payment`,selectedVehicle)
    console.log("차량 결제 정보==>"+res.data);  
    return res.data;
}

//결제정보로 결제 요청->백엔드에 확인 요청 
export const requestBeforePayment=async(settlementPayload)=>{
    const res=await axios.post(`${host}/request-ready-payment`,settlementPayload)
    console.log("res==>"+res.data);
    return res.data;
}
//결제 완료된 경우
export const requestAfterPayment=async(settlementConfirmPayload)=>{
    const res=await axios.post(`${host}/request-after-payment`,settlementConfirmPayload)
    console.log("결제 후 후속처리 요청 정보==>"+res.data)
    return res.data;
}
