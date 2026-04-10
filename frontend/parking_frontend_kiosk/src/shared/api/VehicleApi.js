import axios from "axios"

export const host=(import.meta.env.VITE_API_BASE_URL || '')+'/api/payment'

//keypay로 입력한 차량번호 네글자로 차량 리스트 조회
export const searchCar=async(searchKeyword)=>{
    const res=await axios.post(`${host}/search-car`,{vehicleNumber:searchKeyword})
    return res.data;
}

//사용자가 선택한 차량번호로 결제 정보 조회
export const requestPayment=async(selectedVehicle)=>{
    try{
        const res=await axios.post(`${host}/request-payment`,selectedVehicle)
        console.log("res==>"+res.data);  
        return res.data;
    }catch(error){
        //스프링에서 보내준 에러가 있으면 에러 객체 꺼내기
        const resErr=error.response?.data;

        const customError=new Error(resErr?.message ||"결제 정보 조회 중 오류가 발생하였습니다.")
        customError.code=resErr?.code|| "UNKNOWN_ERROR"

        throw customError
    }
}

//결제정보로 결제 요청->백엔드에 확인 요청 
export const requestBeforePayment=async(settlementPayload)=>{
    try{
        const res=await axios.post(`${host}/request-ready-payment`,settlementPayload)
        console.log("res==>"+res.data);
        return res.data;
    }catch(error){
        const resErr=error?.response?.data;
        const customError=new Error(resErr?.message ||"결제 요청 중 오류가 발생하였습니다.")
        customError.code=resErr?.code|| "UNKNOWN_ERROR"
        //const errMsg=resErr?.message || "결제 요청 중 오류가 발생하였습니다."
        throw customError
    }
}
//결제 완료된 경우
export const requestAfterPayment=async(settlementConfirmPayload)=>{
    try {
        const res=await axios.post(`${host}/request-after-payment`,settlementConfirmPayload)
        console.log("결제 후 후속처리 요청 정보==>"+res.data)
        return res.data;
    } catch (error) {
        const resErr=error?.response?.data;
        const customError=new Error(resErr?.message ||"결제 요청 중 오류가 발생하였습니다.")
        customError.code=resErr?.code|| "UNKNOWN_ERROR"
        //const errMsg=resErr?.message || "결제 요청 중 오류가 발생하였습니다."
        throw customError
    }
}
