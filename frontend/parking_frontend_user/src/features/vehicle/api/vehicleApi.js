import api from "../../auth/api/axios";

/**
 * [차량 관리 API 서비스]
 * 모든 요청은 인증된 상태(Interceptor 적용)에서 이루어집니다.
 */
export const vehicleApi = {
    
    /**
     * 1. 내 차량 정보 조회
     * @returns {Promise<Object|null>} 차량 정보 객체 (없으면 null)
     */
    getMyVehicle: async () => {
        const response = await api.get('/api/user/vehicles/me');
        return response.data;
    },

    /**
     * 2. 차량 등록 신청 (OCR 데이터 포함)
     * @param {Object} registerDate - { carNumber, vehicleName, ocrRawName }
     * @returns {Promise<String>} 성공 메시지
     */
    registerVehicle: async (registerDate) => {
        const response = await api.post('/api/user/vehicles/register', registerDate);
        return response.data;
    },

    /**
     * 3. 승인 대기 중인 신청 취소
     * @param {Long} vehicleId - 취소할 차량 PK
     * @returns {Promise<String>} 성공 메시지
     */
    cancelRegistration: async (vehicleId) => {
        const response = await api.post('/api/user/vehicles/cancel', { vehicleId });
        return response.data;
    },

    /**
     * 4. 활성화된 차량 삭제 (Soft Delete)
     * @param {Long} vehicleId - 삭제할 차량 PK
     */
    deleteVehicle: async (vehicleId) => {
        const response = await api.delete(`/api/user/vehicles/${vehicleId}`);
        return response.data;
    }, // <-- 메서드 구분을 위한 쉼표 추가 완료

    /**
     * 5. 자동차 등록증 OCR 분석 요청
     * @param {File} file - 자동차 등록증 이미지 파일
     * @returns {Promise<Object>} 분석된 차량 정보 (carNumber, vehicleName 등)
     */
    uploadRegistration: async (file) => {
        const formData = new FormData();
        formData.append('file', file);
        const response = await api.post('/api/user/ai/naver/upload-registration', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });
        return response.data;
    },

    /**
     * 6. 신분증 OCR 분석 요청
     * @param {File} file - 신분증 이미지 파일
     * @returns {Promise<Object>} 분석된 개인 정보 (name 등)
     */
    uploadIdCard: async (file) => {
        const formData = new FormData();
        formData.append('file', file);
        const response = await api.post('/api/user/ai/naver/upload-idcard', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });
        return response.data;
    }
};