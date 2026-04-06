// LogoutButton.jsx
import React from 'react'
import useLogout from '../hooks/useLogout';

const LogoutButton = ({ className, children }) => { // children 추가
    const { logout } = useLogout();

    return (
        <button
            type='button'
            onClick={logout}
            className={className || "logout-btn"}
        >
            {children} {}
        </button>
    )
}

export default LogoutButton;