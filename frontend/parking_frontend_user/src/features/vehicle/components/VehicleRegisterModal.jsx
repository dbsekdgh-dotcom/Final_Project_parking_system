import React, { useState } from 'react';
import { useCreateVehicle } from '../hooks/useVehicle';
import Swal from 'sweetalert2';
import { vehicleApi } from '../api/vehicleApi';
import './VehicleRegisterModal.css';

// OCR 생년월일 raw값 → 6자리 숫자 정규화 (e.g. "19901225" → "901225", "90-12-25" → "901225")
const normalizeBirth = (raw) => {
    if (!raw) return '';
    const digits = raw.replace(/[^0-9]/g, '');
    if (digits.length === 8) return digits.substring(2); // YYYYMMDD → YYMMDD
    return digits.substring(0, 6);
};

// OCR 이름 정제 - 반각/전각 괄호 및 내용 제거, 한글만 추출
const cleanName = (raw) => {
    if (!raw) return '';
    return raw
        .replace(/\(.*?\)/g, '')   // 반각 괄호 (...)
        .replace(/（.*?）/g, '')   // 전각 괄호 （...）
        .replace(/[^\uAC00-\uD7A3\s]/g, '') // 한글·공백 외 제거
        .trim();
};

const VehicleRegisterModal = ({ isOpen, onClose }) => {
    const [formData, setFormData] = useState({
        carNumber: '',          // 차량등록증 OCR 차량번호 (표시용)
        vehicleName: '',        // 차량등록증 OCR 차종 (표시용)
        name: '',               // 차량등록증 OCR 성명 (수정 가능)
        birth: '',              // 신분증 OCR 생년월일 (표시용, 6자리)
        ocrRawName: '',         // 차량등록증 OCR 원본 성명 (비교용)
        ocrRawBirth: '',        // 차량등록증 OCR 원본 생년월일 (비교용)
        ocrRawCarNumber: '',    // 차량등록증 OCR 원본 차량번호 (비교용)
        ocrRawVehicleName: '',  // 차량등록증 OCR 원본 차종 (비교용)
        idCardRawName: '',      // 신분증 OCR 원본 성명 (비교용)
        idCardRawBirth: '',     // 신분증 OCR 원본 생년월일 (비교용)
    });

    const [step, setStep] = useState('main');
    const [isOcrLoading, setIsOcrLoading] = useState(false);

    const { mutate: registerVehicle, isLoading: isRegistering } = useCreateVehicle();

    if (!isOpen) return null;

    const handleFileUpload = async (e, type) => {
        const file = e.target.files[0];
        if (!file) return;

        setIsOcrLoading(true);

        try {
            if (type === 'id-card') {
                // 신분증 OCR → name/birth(표시/수정용) + idCardRaw(비교용)
                // idCardRawBirth도 정규화(6자리)로 저장 → 백엔드 birth 비교 시 자리수 일치
                const result = await vehicleApi.uploadIdCard(file);
                const parsedName = cleanName(result.name);
                const normalizedBirth = normalizeBirth(result.birth);
                setFormData(prev => ({
                    ...prev,
                    name: parsedName,
                    birth: normalizedBirth,
                    idCardRawName: result.name || '',
                    idCardRawBirth: normalizedBirth, // 정규화된 6자리로 저장 (birth와 동일 기준)
                }));
                Swal.fire({ icon: 'success', title: '인증 성공', text: '신분증 인증이 완료되었습니다.', confirmButtonText: '확인', confirmButtonColor: '#3085d6' });
            } else {
                // 차량등록증 OCR → carNumber/vehicleName(표시용) + ocrRaw(비교용)
                const result = await vehicleApi.uploadRegistration(file);
                setFormData(prev => ({
                    ...prev,
                    carNumber: result.carNumber || '',
                    vehicleName: result.vehicleName || '',
                    ocrRawName: result.ocrRawName || '',
                    ocrRawBirth: result.orcRawBirth || '',       // 백엔드 오타: orcRawBirth
                    ocrRawCarNumber: result.carNumber || '',     // OCR 원본 차량번호
                    ocrRawVehicleName: result.vehicleName || '', // OCR 원본 차종
                }));
                Swal.fire({ icon: 'success', title: '추출 성공', text: '차량 정보가 입력되었습니다.', confirmButtonText: '확인', confirmButtonColor: '#3085d6' });
            }
            setStep('main');
        } catch (error) {
            // 1. 서버가 보낸 에러 데이터 추출 (객체일 수도, 문자열일 수도 있음)
            const serverData = error?.response?.data;
            
            // 2. 메시지 추출 (data.message가 있으면 쓰고, 없으면 data 통째로 문자열화)
            const displayMessage = typeof serverData === 'object' 
                ? (serverData.message || JSON.stringify(serverError)) 
                : (serverData || '이미지 분석 중 알 수 없는 오류가 발생했습니다.');

            console.error("OCR 분석 에러 상세:", serverData);

            Swal.fire({
                icon: 'error',
                title: '분석 실패',
                text: displayMessage, // ⬅️ 이제 여기에 "NAVER_OCR_API_FAIL..."이 뜹니다!
                confirmButtonColor: '#3085d6',
                confirmButtonText: '확인'
            });
        } finally {
            setIsOcrLoading(false);
}
    };

    const handleSubmit = () => {
        const {
            carNumber, vehicleName, name, birth,
            ocrRawName, ocrRawBirth, ocrRawCarNumber, ocrRawVehicleName,
            idCardRawName, idCardRawBirth,
        } = formData;

        if (!idCardRawName || !idCardRawBirth) {
            Swal.fire({
                icon: 'warning',
                title: '신분증 인증 필요',
                text: '신분증 인증을 먼저 완료해주세요.',
                confirmButtonText: '확인',
                confirmButtonColor: '#3085d6',
            });
            return;
        }
        if (!ocrRawCarNumber || !ocrRawName) {
            Swal.fire({
                icon: 'warning',
                title: '차량등록증 인증 필요',
                text: '차량등록증 인증을 먼저 완료해주세요.',
                confirmButtonText: '확인',
                confirmButtonColor: '#3085d6',
            });
            return;
        }

        registerVehicle(
            { carNumber, vehicleName, name, birth, ocrRawName, ocrRawBirth, ocrRawCarNumber, ocrRawVehicleName, idCardRawName, idCardRawBirth },
            { onSuccess: () => onClose() }
        );
    };

    const stepTitle = {
        main: '차량 등록',
        'id-card': '신분증 인증',
        'car-paper': '차량등록증 인증',
    };

    return (
        <div className="modal-overlay">
            <div className="modal-card">

                {/* 헤더 */}
                <div className="modal-header">
                    <h2 className="modal-header__title">{stepTitle[step]}</h2>
                    <button className="modal-close-btn" onClick={onClose}>&times;</button>
                </div>

                {/* Step 1: 메인 폼 */}
                {step === 'main' && (
                    <div className="modal-body">
                        <div className="modal-field">
                            <label className="modal-label">차량 번호 {formData.ocrRawCarNumber && '✅'}</label>
                            <input
                                className="modal-input"
                                value={formData.carNumber}
                                placeholder="차량등록증을 첨부해주세요"
                                onChange={(e) => setFormData(prev => ({ ...prev, carNumber: e.target.value }))}
                            />
                        </div>
                        <div className="modal-field">
                            <label className="modal-label">차량 모델 {formData.ocrRawVehicleName && '✅'}</label>
                            <input
                                className="modal-input"
                                value={formData.vehicleName}
                                placeholder="차량등록증을 첨부해주세요"
                                onChange={(e) => setFormData(prev => ({ ...prev, vehicleName: e.target.value }))}
                            />
                        </div>
                        <div className="modal-field">
                            <label className="modal-label">성함 {formData.idCardRawName && '✅'}</label>
                            <input
                                className="modal-input"
                                value={formData.name}
                                placeholder="신분증을 첨부해주세요"
                                onChange={(e) => setFormData(prev => ({ ...prev, name: e.target.value }))}
                            />
                        </div>
                        <div className="modal-field">
                            <label className="modal-label">생년월일 {formData.idCardRawBirth && '✅'}</label>
                            <input
                                className="modal-input"
                                value={formData.birth}
                                placeholder="신분증을 첨부해주세요 (6자리)"
                                onChange={(e) => setFormData(prev => ({ ...prev, birth: e.target.value }))}
                            />
                        </div>

                        {/* 신분증 또는 차량등록증 인증 완료 시 경고 문구 */}
                        {(formData.idCardRawName || formData.ocrRawCarNumber) && (
                            <div className="modal-ocr-notice">
                                AI가 자동으로 읽어온 정보입니다. 잘못된 내용이 있으면 수정 후 신청해주세요.
                                <br />
                                <strong>잘못된 정보로 신청 시 승인이 거절될 수 있습니다.</strong>
                            </div>
                        )}

                        <div className="modal-upload-section">
                            <span className="modal-upload-label">서류 첨부 *</span>
                            <div className="modal-upload-row">
                                <div className="upload-box" onClick={() => setStep('id-card')}>
                                    <span>🪪</span>
                                    <span>신분증</span>
                                </div>
                                <div className="upload-box" onClick={() => setStep('car-paper')}>
                                    <span>📄</span>
                                    <span>차량등록증</span>
                                </div>
                            </div>
                        </div>
                    </div>
                )}

                {/* Step 2 & 3: 파일 업로드 */}
                {(step === 'id-card' || step === 'car-paper') && (
                    <div className="modal-body">
                        <p className="upload-step__desc">
                            {step === 'id-card' ? '신분증' : '차량등록증'} 사진을 업로드해주세요.
                        </p>

                        <label className={`upload-step__dropzone${isOcrLoading ? ' upload-step__dropzone--loading' : ''}`}>
                            {isOcrLoading ? (
                                <div className="upload-step__dropzone--loading">
                                    <div className="upload-step__spinner">⏳</div>
                                    <p className="upload-step__loading-text">AI 분석 중...</p>
                                </div>
                            ) : (
                                <span className="upload-step__placeholder">이미지 선택 (클릭)</span>
                            )}
                            <input
                                type="file"
                                accept="image/*"
                                onChange={(e) => handleFileUpload(e, step)}
                                style={{ display: 'none' }}
                                disabled={isOcrLoading}
                            />
                        </label>
                    </div>
                )}

                {/* 푸터 버튼 */}
                <div className="modal-footer">
                    {step === 'main' ? (
                        <>
                            <button
                                className="btn-modal-submit"
                                onClick={handleSubmit}
                                disabled={isRegistering}
                            >
                                {isRegistering ? '등록 중...' : '등록 신청'}
                            </button>
                            <button className="btn-modal-cancel" onClick={onClose}>
                                취소
                            </button>
                        </>
                    ) : (
                        <>
                            <button
                                className="btn-modal-submit"
                                onClick={() => setStep('main')}
                                disabled={isOcrLoading}
                            >
                                확인
                            </button>
                            <button
                                className="btn-modal-cancel"
                                onClick={() => setStep('main')}
                                disabled={isOcrLoading}
                            >
                                취소
                            </button>
                        </>
                    )}
                </div>

            </div>
        </div>
    );
};

export default VehicleRegisterModal;
