import React from "react";
import { Navigate } from "react-router-dom";


export const PublicRoute = ({ children }) => {
    const token = localStorage.getItem("accessToken");

    if(token) {
        return <Navigate to="/dashboard" replace/>
    }
    return children;
};

export const PrivateRoute = ({ children }) => {
    const token = localStorage.getItem("accessToken");

    if(!token) {
        return <Navigate to="/" replace />
    }
    return children;
}