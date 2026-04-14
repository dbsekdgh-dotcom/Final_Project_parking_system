import React from "react";
import "./UserStatusCard.css";
import ResidentCancelButton from "../../apply/components/ResidentCancelButton";

/**
 * 백엔드 statusCode에 따른 UI 설정
 * RESIDENT: 입주 완료
 * PENDING: 신청 대기 중
 * NONE: 신청 이력 없음 (일반 회원)
 */
const STATUS_CONFIG = {
    "RESIDENT": { 
        badge: "입주민", 
        badgeClass: "badge--resident", 
        desc: "입주민으로 등록되어 모든 서비스를 이용하실 수 있습니다." 
    },
    "PENDING": { 
        badge: "신청 중", 
        badgeClass: "badge--pending", 
        desc: "입주민 등록 신청이 접수되었습니다. 관리자 승인을 기다려주세요." 
    },
    "NONE": { 
        badge: "일반 회원", 
        badgeClass: "badge--general", 
        desc: "아직 입주민 등록이 되어있지 않습니다. 아래에서 신청하실 수 있습니다." 
    },
};

const MemberStatusCard = ({ userStatus, approvalId }) => {
    const config = STATUS_CONFIG[userStatus] || STATUS_CONFIG["NONE"];

    // 3. 현재 신청 대기 중(PENDING)인지 확인
    const isPending = userStatus === "PENDING";

    return (
        <div className="status-card">
            <div className="status-card__header">
                <h3 className="status-card__title">회원 현황</h3>
            </div>
            
            <div className="status-card__body">
                <div className="status-card__content">
                    {/* 배지 표시 */}
                    <span className={`status-badge ${config.badgeClass}`}>
                        {config.badge}
                    </span>
                    
                    {/* 상태 설명 텍스트 */}
                    <p className="status-card__desc">
                        {config.desc}
                    </p>
                </div>

                {/* 4. 신청 대기 중이고 취소할 ID가 있을 때만 취소 버튼 노출 */}
                {isPending && approvalId && (
                    <div className="status-card__actions">
                        <ResidentCancelButton approvalId={approvalId} />
                    </div>
                )}
            </div>
        </div>
    );
};

export default MemberStatusCard;