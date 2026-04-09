import React from 'react';
import { checkEmailAvailability } from '../utils/accountUtils';

/**
 * EmailInputField 컴포넌트
 * - 이메일 입력 및 중복 확인 기능을 담당합니다.
 * - 탈퇴한 계정의 경우 accountUtils의 로직에 따라 복구 모달을 띄웁니다.
 */
const EmailInputField = ({ email, setEmail, isEmailFixed, setIsEmailFixed }) => {

    // [중복 확인] 버튼 클릭 시 실행
    const handleCheckEmail = async () => {
        // checkEmailAvailability 함수 내부에서:
        // 1. 사용 가능하면 true 반환
        // 2. 탈퇴 계정이면 handleLocalRecover(복구 모달) 실행 후 false 반환
        // 3. 중복이면 알림 후 false 반환
        const isAvailable = await checkEmailAvailability(email);
        
        if (isAvailable) {
            // 진짜 사용 가능한 이메일일 때만 입력창을 잠금(Fixed) 상태로 변경
            setIsEmailFixed(true);
        }
    };

    // [수정하기] 버튼 클릭 시 실행
    const handleEditEmail = () => {
        setIsEmailFixed(false);
    };

    return (
        <div className="emailFieldContainer" style={{ marginBottom: "15px" }}>
            <label className="fieldLabel" htmlFor="email" style={{ display: "block", marginBottom: "8px", fontWeight: "bold" }}>
                이메일
            </label>
            <div style={{ display: "flex", gap: "8px" }}>
                <input
                    id="email"
                    type="email"
                    placeholder="example@email.com"
                    className={`fieldInput ${isEmailFixed ? "readOnlyInput" : ""}`}
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    readOnly={isEmailFixed}
                    required
                    style={{
                        flex: 1,
                        padding: "10px",
                        borderRadius: "8px",
                        border: "1px solid #ccc",
                        backgroundColor: isEmailFixed ? "#f5f5f5" : "white"
                    }}
                />
                
                {!isEmailFixed ? (
                    <button 
                        type="button" 
                        className="checkButton" 
                        onClick={handleCheckEmail}
                        style={{ 
                            width: "110px", 
                            padding: "10px", 
                            borderRadius: "8px", 
                            border: "none", 
                            cursor: "pointer", 
                            backgroundColor: "#3085d6", 
                            color: "white",
                            fontWeight: "bold"
                        }}
                    >
                        중복 확인
                    </button>
                ) : (
                    <button 
                        type="button" 
                        className="editButton" 
                        onClick={handleEditEmail}
                        style={{ 
                            width: "110px", 
                            padding: "10px", 
                            borderRadius: "8px", 
                            border: "none", 
                            cursor: "pointer", 
                            backgroundColor: "#757575", 
                            color: "white",
                            fontWeight: "bold"
                        }}
                    >
                        수정하기
                    </button>
                )}
            </div>
            {isEmailFixed && (
                <p style={{ color: "#2ecc71", fontSize: "12px", marginTop: "5px", marginLeft: "2px" }}>
                    인증이 완료된 이메일입니다.
                </p>
            )}
        </div>
    );
};

export default EmailInputField;