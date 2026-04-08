import axios from "axios"

export const host=(import.meta.env.VITE_API_BASE_URL || '')+'/api/payment'

//keypay로 입력한 차량번호 네글자로 차량 리스트 조회
export const searchCar=async(searchKeyword)=>{
    const res=await axios.post(`${host}/search-car`,{vehicleNumber:searchKeyword})
    return res.data;
}

//사용자가 선택한 차량번호로 결제 정보 조회
export const requestPrepayment=async(selectedVehicle)=>{
    try{
        const res=await axios.post(`${host}/request-payment`,selectedVehicle)
        console.log("res==>"+res.data);  
        return res.data;
    }catch(error){
        //스프링에서 보내준 에러가 있으면 에러 객체 꺼내기
        const resErr=error.response?.data;

        //에러 메세지 추출(스프링에서 정의한 예외 외의 상황에는 기본 메세지)
        const errMsg=resErr?.message || "결제 정보 조회 중 오류가 발생하였습니다."
        throw new Error(errMsg)
    }
}

