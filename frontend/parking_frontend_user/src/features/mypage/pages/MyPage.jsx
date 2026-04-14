import React from "react";
import { useQuery } from "@tanstack/react-query";
import { fetchMyInfo } from "../api/mypageApi";
import { fetchUserStatus } from "../../apply/api/applyApi";
import UserInfoCard from "../components/UserInfoCard";
import SecuritySection from "../components/SecuritySection";
import UserStatusCard from "../components/UserStatusCard";
import ResidentApplySection from "../../apply/components/ResidentApplySection";
import "./MyPage.css";

const MyPage = () => {
    const { data: userInfo, isLoading: isInfoLoading } = useQuery({
        queryKey: ["myInfo"],
        queryFn: fetchMyInfo,
    });

    const { data: statusInfo, isLoading: isStatusLoading } = useQuery({
        queryKey: ["userStatus"], 
        queryFn: fetchUserStatus,
    });

    if (isInfoLoading || isStatusLoading) {
        return <div className="mypage-loading">데이터를 불러오는 중...</div>;
    }

    return (
        <div className="mypage">
            <h2 className="mypage__title">마이페이지</h2>
            <div className="mypage__grid">
                <UserInfoCard userInfo={userInfo} />
                
                <SecuritySection
                    currentPhone={userInfo?.phone}
                    userName={userInfo?.name}
                    userEmail={userInfo?.email}
                />

                {/* 백엔드 코드(PENDING 등)와 승인ID 전달 */}
                <UserStatusCard 
                    userStatus={statusInfo?.userStatus} 
                    approvalId={statusInfo?.activeApprovalId} 
                />

                {/* 상태 코드를 그대로 넘겨서, 신청 폼 노출 여부 결정 */}
                <ResidentApplySection memberStatus={statusInfo?.userStatus} />
            </div>
        </div>
    );
};

export default MyPage;