import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { vehicleApi } from "../api/vehicleApi"
import Swal from "sweetalert2";

/**
 * [내 차량 정보 조회 훅]
 * - 역할: 서버에서 내 차량의 현재 상태(정보 없음, 승인 대기, 등록 완료 등)를 읽어옵니다.
 * - 특징: staleTime 설정을 통해 5분간 데이터를 캐싱하여 불필요한 재요청을 방지합니다.
 */
export const useMyVehicle = () => {
    return useQuery({
        queryKey: ['myVehicle'],
        queryFn: vehicleApi.getMyVehicle,
        staleTime: 0,
    });
};

/**
 * [차량 등록 신청 훅]
 * - 역할: OCR 데이터와 차량 별칭을 서버에 저장하여 새로운 차량 등록을 요청합니다.
 * - 특징: 성공 시 'myVehicle' 쿼리를 무효화하여 등록된 정보를 즉시 화면에 반영합니다.
 */
export const useCreateVehicle = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: vehicleApi.registerVehicle,
        onSuccess: (data) => {
            // 등록 성공 시 조회 쿼리를 무효화하여 최신 상태로 갱신
            queryClient.invalidateQueries({ queryKey: ['myVehicle'] });

            Swal.fire({
                icon: 'success',
                title: '등록 요청 완료',
                text: '차량등록 요청이 정상적으로 처리되었습니다.',
                confirmButtonText: '확인',
                confirmButtonColor: '#3085d6',
            });
        },
        onError: (error) => {
            Swal.fire({
                icon: 'error',
                title: '등록 실패',
                text: error.response?.data?.message || 'OCR 인식 또는 등록 중 오류가 발생했습니다.',
                confirmButtonText: '확인',
                confirmButtonColor: '#d33',
            });
        }
    });
};

/**
 * [등록 신청 취소 훅]
 * - 역할: 승인 대기(PENDING) 상태인 차량의 등록 신청을 철회합니다.
 * - 특징: 성공 시 차량 정보를 다시 조회하여 화면에서 대기 중인 정보를 제거합니다.
 */
export const useCancelVehicle = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: vehicleApi.cancelRegistration,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['myVehicle'] });

            Swal.fire({
                icon: 'success',
                title: '취소 완료',
                text: '차량 등록 신청이 취소되었습니다.',
                confirmButtonText: '확인',
                confirmButtonColor: '#3085d6',
            });
        },
        onError: (error) => {
            Swal.fire({
                icon: 'error',
                title: '취소 실패',
                text: error.response?.data?.message || '취소 처리 중 오류가 발생했습니다.',
                confirmButtonText: '확인',
                confirmButtonColor: '#d33',
            });
        }
    });
};

/**
 * [차량 삭제 훅]
 * - 역할: 등록된 차량(ACTIVE)을 시스템상에서 삭제(Soft Delete) 처리합니다.
 * - 특징: 주차 중이거나 정기권이 있는 경우 백엔드 예외 메시지를 Swal로 출력합니다.
 */
export const useDeleteVehicle = () => {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: vehicleApi.deleteVehicle,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['myVehicle'] });

            Swal.fire({
                icon: 'success',
                title: '삭제 완료',
                text: '차량이 성공적으로 삭제되었습니다.',
                confirmButtonText: '확인',
                confirmButtonColor: '#3085d6',
            });
        },
        onError: (error) => {
            Swal.fire({
                icon: 'error',
                title: '삭제 실패',
                text: error.response?.data?.message || '차량 삭제 중 오류가 발생했습니다.',
                confirmButtonText: '확인',
                confirmButtonColor: '#d33',
            });
        }
    });
};

/**
 * [신분증 OCR 분석 훅]
 * - 역할: 신분증 이미지를 서버로 보내 분석 결과를 받아옵니다.
 * - 특징: 서버 에러 발생 시 상세 메시지(네이버 응답 코드 등)를 Swal에 직접 노출합니다.
 */
export const useUploadIdCard = () => {
    return useMutation({
        mutationFn: vehicleApi.uploadIdCard,
        onSuccess: (data) => {
            // 성공 시 로직 (예: 상태 저장 등)
            console.log("OCR 결과:", data);
        },
        onError: (error) => {
            // 서버에서 보낸 에러 데이터 추출
            const serverError = error.response?.data;
            
            // 상세 로그 출력 (개발자 도구에서 확인용)
            console.error("OCR 분석 에러 발생:", serverError);

            Swal.fire({
                icon: 'error',
                title: 'OCR 분석 실패',
                // 서버가 보낸 에러가 JSON 객체면 문자열로 변환하고, 
                // 그냥 문자열(RuntimeException 메시지)이면 그대로 출력합니다.
                text: typeof serverError === 'object'
                    ? (serverError.message || JSON.stringify(serverError))
                    : (serverError || '네트워크 연결이 원활하지 않습니다.'),
                confirmButtonText: '확인',
                confirmButtonColor: '#d33',
            });
        }
    });
};

/**
 * [자동차 등록증 OCR 분석 훅]
 */
export const useUploadRegistration = () => {
    return useMutation({
        mutationFn: vehicleApi.uploadRegistration,
        onSuccess: (data) => {
            console.log("자동차 등록증 결과:", data);
        },
        onError: (error) => {
            const serverError = error.response?.data;
            console.error("등록증 분석 에러:", serverError);

            Swal.fire({
                icon: 'error',
                title: '등록증 분석 실패',
                text: typeof serverError === 'object'
                    ? (serverError.message || JSON.stringify(serverError))
                    : (serverError || '파일 형식을 확인해주세요.'),
                confirmButtonText: '확인',
                confirmButtonColor: '#d33',
            });
        }
    });
};