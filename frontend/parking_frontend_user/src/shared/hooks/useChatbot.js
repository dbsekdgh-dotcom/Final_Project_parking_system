import { useState, useCallback } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { sendChatMessage } from '../api/chatbotApi';

export const useChatbot = () => {
    const queryClient = useQueryClient();
    const [messages, setMessages] = useState([
        {
            id: 1,
            role: 'bot',
            text: '안녕하세요! AI 주차 비서입니다 🚗\n방문예약, 주차 현황, 포인트, 정기권 등을 도와드릴게요.',
        },
    ]);
    const [input, setInput] = useState('');
    const [loading, setLoading] = useState(false);

    const sendMessage = useCallback(async () => {
        const trimmed = input.trim();
        if (!trimmed || loading) return;

        const userMsg = { id: Date.now(), role: 'user', text: trimmed };
        setMessages((prev) => [...prev, userMsg]);
        setInput('');
        setLoading(true);

        try {
            const history = messages
                .filter((m) => m.id !== 1)
                .map((m) => ({ role: m.role, text: m.text }));
            const data = await sendChatMessage(trimmed, history);
            const botMsg = { id: Date.now() + 1, role: 'bot', text: data.reply, reservations: data.reservations || null };
            setMessages((prev) => [...prev, botMsg]);
            if (data.action === 'RESERVATION_CREATED' || data.action === 'RESERVATION_CANCELLED') {
                queryClient.invalidateQueries({ queryKey: ['myReservations'] });
                queryClient.invalidateQueries({ queryKey: ['reservationPolicy'] });
            }
        } catch {
            const errMsg = {
                id: Date.now() + 1,
                role: 'bot',
                text: '죄송합니다, 잠시 후 다시 시도해 주세요.',
            };
            setMessages((prev) => [...prev, errMsg]);
        } finally {
            setLoading(false);
        }
    }, [input, loading]);

    return { messages, input, setInput, loading, sendMessage };
};
