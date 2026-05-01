import React, { useEffect, useRef } from 'react';
import { useChatbot } from '../../hooks/useChatbot';
import ChatReservationTable from './ChatReservationTable';
import './ChatModal.css';

const ChatModal = ({ onClose }) => {
    const { messages, input, setInput, loading, sendMessage } = useChatbot();
    const bottomRef = useRef(null);
    const inputRef = useRef(null);

    useEffect(() => {
        bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [messages]);

    useEffect(() => {
        if (!loading) inputRef.current?.focus();
    }, [loading]);

    const handleKeyDown = (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    };

    return (
        <div className="chat-modal">
            <div className="chat-modal__header">
                <span>AI 주차 비서</span>
                <button className="chat-modal__close" onClick={onClose}>✕</button>
            </div>

            <div className="chat-modal__body">
                {messages.map((msg) => (
                    <div key={msg.id} className={`chat-bubble chat-bubble--${msg.role}`}>
                        <span className="chat-bubble__text">
                            {msg.reservations ? msg.text.split('\n')[0] : msg.text}
                        </span>
                        {msg.reservations && <ChatReservationTable reservations={msg.reservations} />}
                    </div>
                ))}
                {loading && (
                    <div className="chat-bubble chat-bubble--bot">
                        <span className="chat-bubble__typing">답변 중...</span>
                    </div>
                )}
                <div ref={bottomRef} />
            </div>

            <div className="chat-modal__footer">
                <input
                    ref={inputRef}
                    className="chat-modal__input"
                    type="text"
                    placeholder="메시지를 입력하세요"
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    onKeyDown={handleKeyDown}
                    disabled={loading}
                />
                <button
                    className="chat-modal__send"
                    onClick={sendMessage}
                    disabled={loading || !input.trim()}
                >
                    전송
                </button>
            </div>
        </div>
    );
};

export default ChatModal;