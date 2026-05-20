import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import ChatModal from './ChatModal';
import './ChatButton.css';

const ChatButton = () => {
    const [isOpen, setIsOpen] = useState(false);
    const location = useLocation();

    useEffect(() => {
        setIsOpen(false);
    }, [location.pathname]);

    return (
        <>
            {isOpen && <ChatModal onClose={() => setIsOpen(false)} />}
            <button
                className={`chat-fab${isOpen ? ' chat-fab--open' : ''}`}
                onClick={() => setIsOpen((prev) => !prev)}
                aria-label="AI 주차 비서 열기"
            >
                <span className="chat-fab__icon">💬</span>
                <span className="chat-fab__label">AI 비서</span>
            </button>
        </>
    );
};

export default ChatButton;
