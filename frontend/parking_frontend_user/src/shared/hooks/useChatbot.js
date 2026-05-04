import { useState, useCallback } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { sendChatMessage } from '../api/chatbotApi';

export const useChatbot = (onClose) => {
    const queryClient = useQueryClient();
    const navigate = useNavigate();
    const [messages, setMessages] = useState([
        {
            id: 1,
            role: 'bot',
            text: '안녕하세요! AI 주차 비서입니다 🚗\n방문예약, 주차 현황, 포인트, 정기권, 차량 등록, 입주민 신청 등을 도와드릴게요.',
        },
    ]);
    const [input, setInput] = useState('');
    const [loading, setLoading] = useState(false);

    const doSend = useCallback(async (text, currentMessages) => {
        const userMsg = { id: Date.now(), role: 'user', text };
        setMessages((prev) => [...prev, userMsg]);
        setLoading(true);

        try {
            const history = currentMessages
                .filter((m) => m.id !== 1)
                .map((m) => ({ role: m.role, text: m.text }));
            const data = await sendChatMessage(text, history);
            const botMsg = { id: Date.now() + 1, role: 'bot', text: data.reply, reservations: data.reservations || null, units: data.availableUnits || null };
            setMessages((prev) => [...prev, botMsg]);
            if (data.action === 'RESERVATION_CREATED' || data.action === 'RESERVATION_CANCELLED') {
                queryClient.invalidateQueries({ queryKey: ['myReservations'] });
                queryClient.invalidateQueries({ queryKey: ['reservationPolicy'] });
            }
            if (data.action === 'RESIDENT_APPLIED' || data.action === 'RESIDENT_CANCELLED') {
                queryClient.invalidateQueries({ queryKey: ['userStatus'] });
                queryClient.invalidateQueries({ queryKey: ['unitStatus'] });
                queryClient.invalidateQueries({ queryKey: ['myInfo'] });
            }
            if (data.action === 'SUBSCRIPTION_PURCHASE') {
                const date = data.subscriptionStartDate ? `?date=${data.subscriptionStartDate}` : '';
                setTimeout(() => {
                    onClose?.();
                    navigate(`/subscription${date}`);
                }, 1200);
            }
            if (data.action === 'VEHICLE_REGISTER') {
                onClose?.();
                navigate('/mypage?openVehicle=1');
            }
            if (data.action === 'VEHICLE_REGISTER_CANCELLED') {
                queryClient.invalidateQueries({ queryKey: ['myVehicle'] });
            }
        } catch {
            setMessages((prev) => [...prev, { id: Date.now() + 1, role: 'bot', text: '죄송합니다, 잠시 후 다시 시도해 주세요.' }]);
        } finally {
            setLoading(false);
        }
    }, [queryClient, navigate]);

    const sendMessage = useCallback(async () => {
        const trimmed = input.trim();
        if (!trimmed || loading) return;
        setInput('');
        await doSend(trimmed, messages);
    }, [input, loading, messages, doSend]);

    const sendDirect = useCallback(async (text) => {
        if (loading) return;
        await doSend(text, messages);
    }, [loading, messages, doSend]);

    return { messages, input, setInput, loading, sendMessage, sendDirect };
};
