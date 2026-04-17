import React from "react";
import { useQuery } from "@tanstack/react-query";
import { fetchMyInfo } from "../api/mypageApi";
import { fetchUserStatus } from "../../apply/api/applyApi";
import UserInfoCard from "../components/UserInfoCard";
import SecuritySection from "../components/SecuritySection";
import UserStatusCard from "../components/UserStatusCard";
import ResidentApplySection from "../../apply/components/ResidentApplySection";
import "./MyPage.css";
import VehicleCard from "../../vehicle/components/VehicleCard";

const MyPage = () => {
    // 1. 회원 정보 조회
    const { data: userInfo, isLoading: isInfoLoading } = useQuery({
        queryKey: ["myInfo"],
        queryFn: fetchMyInfo,
    });

    // 2. 입주 신청 상태 조회
    const { data: statusInfo, isLoading: isStatusLoading } = useQuery({
        queryKey: ["userStatus"],
        queryFn: fetchUserStatus,
    });

    // 로딩 처리
    if (isInfoLoading || isStatusLoading) {
        return <div className="mypage-loading">데이터를 불러오는 중...</div>;
    }

    return (
        <div className="mypage">
            <h2 className="mypage__title">마이페이지</h2>
            <div className="mypage__grid">
                {/* [좌측상단] 기본 회원 정보 */}
                <UserInfoCard userInfo={userInfo} />
                
                {/* [우측상단] 보안 및 개인정보 수정 */}
                <SecuritySection
                    currentPhone={userInfo?.phone}
                    userName={userInfo?.name}
                    userEmail={userInfo?.email}
                />

                {/* [좌측하단] 입주 신청 상태 현황 */}
                <UserStatusCard 
                    userStatus={statusInfo?.userStatus} 
                    approvalId={statusInfo?.activeApprovalId} 
                />

                {/* [우측하단] 차량 관리 (OCR 등록/조회/삭제) */}
                <VehicleCard />

                {/* [하단 전체] 거주 신청 섹션 (상태에 따라 폼 노출) */}
                <div className="mypage__full-width">
                    <ResidentApplySection memberStatus={statusInfo?.userStatus} />
                </div>
            </div>
        </div>
    );
};

export default MyPage;