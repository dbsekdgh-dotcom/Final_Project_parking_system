import adminApi from "../../../shared/api/adminApi";

//관리자 - 주차공간 상단 요약정보
export const getParkingSpaceSummary=async()=>{
    try{
        const response = await adminApi.get('/parking-space/summary')
        return response.data
    }catch(error){
        console.error("주차공간 요약 데이터 로드 실패:",error)
        throw error
    }
}

//관리자 - 주차공간 하단 층별 주차공간 리스트 정보
export const getParkingSpace =async(floor)=>{
    try{
        const response = await adminApi.get(`/parking-space`,{params:{floor}})
        return response.data
    }catch(error){
        console.error(`${floor}층 주차 데이터 로드 실패:`,error)
        throw error
    }
}