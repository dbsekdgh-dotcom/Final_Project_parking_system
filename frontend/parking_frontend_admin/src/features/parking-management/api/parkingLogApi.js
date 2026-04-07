import adminApi from "../../../shared/api/adminApi";

// 관리자 - 입출차기록 상단 요약 정보 조회
export const getParkingLogSummary = async()=> {
    try {
        const response = await adminApi.get('/parking/summary');
        return response.data;
    }catch(error){
        console.error("입출차 요약 데이터 로드 실패:",error);
        throw error;
    }
}

// 관리자 - 입출차기록 하단 내역 테이블 조회 (검색+페이징)
export const getParkingLogList = async(keyword='',page=0,size=5) => {
    try {
        const response = await adminApi.get('/parking/logs',{
            params:{
                keyword,page,size,
                sort: 'parkingLogId,desc'
            }
        });
        return response.data;
    }catch(error){
        console.error("입츨차 목록 데이터 로드 실패:",error);
        throw error;
    }
} 