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
        carNumber: '',
        vehicleName: '',
        name: '',
        birth: '',
        ocrRawName: '',
        ocrRawBirth: '',
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
                // 신분증 OCR → name, birth 추출
                const result = await vehicleApi.uploadIdCard(file);
                const parsedName = cleanName(result.name);
                setFormData(prev => ({
                    ...prev,
                    name: parsedName,
                    ocrRawName: result.name || '',
                    birth: normalizeBirth(result.birth),
                    ocrRawBirth: result.birth || '',
                }));
                Swal.fire('인증 성공', `${result.name}님 확인되었습니다.`, 'success');
            } else {
                // 차량등록증 OCR → carNumber, vehicleName 추출
                const result = await vehicleApi.uploadRegistration(file);
                setFormData(prev => ({
                    ...prev,
                    carNumber: result.carNumber || '',
                    vehicleName: result.vehicleName || '',
                }));
                Swal.fire('추출 성공', '차량 정보가 입력되었습니다.', 'success');
            }
            setStep('main');
        } catch (error) {
            console.error(error);
            Swal.fire('분석 실패', '이미지 분석 중 오류가 발생했습니다.', 'error');
        } finally {
            setIsOcrLoading(false);
        }
    };

    const handleSubmit = () => {
        const { carNumber, vehicleName, name, birth, ocrRawName, ocrRawBirth } = formData;

        if (!ocrRawName || !ocrRawBirth) {
            Swal.fire({
                icon: 'warning',
                title: '신분증 인증 필요',
                text: '신분증 인증을 먼저 완료해주세요.',
                confirmButtonColor: '#3085d6',
            });
            return;
        }
        if (!carNumber || !vehicleName) {
            Swal.fire({
                icon: 'warning',
                title: '차량등록증 인증 필요',
                text: '차량등록증 인증을 먼저 완료해주세요.',
                confirmButtonColor: '#3085d6',
            });
            return;
        }

        registerVehicle({ carNumber, vehicleName, name, birth, ocrRawName, ocrRawBirth }, {
            onSuccess: () => onClose(),
        });
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
                            <label className="modal-label">차량 번호 {formData.carNumber && '✅'}</label>
                            <input className="modal-input" value={formData.carNumber} placeholder="차량등록증을 첨부해주세요" readOnly />
                        </div>
                        <div className="modal-field">
                            <label className="modal-label">차량 모델 {formData.vehicleName && '✅'}</label>
                            <input className="modal-input" value={formData.vehicleName} placeholder="차량등록증을 첨부해주세요" readOnly />
                        </div>
                        <div className="modal-field">
                            <label className="modal-label">인증 성함 {formData.ocrRawName && '✅'}</label>
                            <input
                                className="modal-input"
                                value={formData.name}
                                placeholder="신분증을 첨부해주세요"
                                onChange={(e) => setFormData(prev => ({ ...prev, name: e.target.value }))}
                            />
                        </div>
                        <div className="modal-field">
                            <label className="modal-label">생년월일 {formData.birth && '✅'}</label>
                            <input className="modal-input" value={formData.birth} placeholder="신분증을 첨부해주세요 (6자리)" readOnly />
                        </div>

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
