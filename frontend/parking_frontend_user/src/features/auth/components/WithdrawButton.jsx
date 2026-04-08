import React from 'react';
import { useNavigate } from 'react-router-dom';
import { handleWithdraw } from "../utils/accountUtils.js";
const WithdrawButton = () => {
    const navigate = useNavigate();

    return (
        <button
            type="button"
            onClick={() => handleWithdraw(navigate)} // navigate를 인자로 전달
            className="sidebar__logout sidebar__withdraw"
        >
            회원 탈퇴
            <span aria-hidden>🚪</span>
        </button>
    );
}

export default WithdrawButton;