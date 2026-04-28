import React from "react";
import "./UserInfoCard.css";

const UserInfoCard = ({ userInfo }) => {
    return (
        <div className="info-card">
            <h3 className="info-card__title">기본 정보</h3>

            <div className="info-row">
                <span className="info-label">이름</span>
                <span className="info-value">{userInfo?.name ?? "-"}</span>
            </div>

            <div className="info-row">
                <span className="info-label">이메일</span>
                <span className="info-value">{localStorage.getItem("userEmail") ?? "-"}</span>
            </div>

            <div className="info-row">
                <span className="info-label">전화번호</span>
                <span className="info-value">{userInfo?.phone ?? "-"}</span>
            </div>

            <div className="info-row">
                <span className="info-label">생년월일</span>
                <span className="info-value">{userInfo?.birth ?? "-"}</span>
            </div>

            {userInfo?.unitNo != null && (
                <div className="info-row">
                    <span className="info-label">거주 세대</span>
                    <span className="info-value">{userInfo.unitNo}호</span>
                </div>
            )}
        </div>
    );
};

export default UserInfoCard;
