import React from "react";
import { Navigate } from "react-router-dom";

// sessionStorage는 브라우저 종료 시 자동 삭제됨 → 재로그인 필요
// localStorage는 UI 데이터 보관용 (userName, userStatus 등)

export const PublicRoute = ({ children }) => {
    const isLoggedIn = !!sessionStorage.getItem("sessionActive");

    if (isLoggedIn) {
        return <Navigate to="/dashboard" replace />;
    }
    return children;
};

export const PrivateRoute = ({ children }) => {
    const isLoggedIn = !!sessionStorage.getItem("sessionActive");

    if (!isLoggedIn) {
        return <Navigate to="/" replace />;
    }
    return children;
};
