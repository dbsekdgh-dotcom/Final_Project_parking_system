import { useCallback, useEffect, useRef } from "react";
import { useWebSocket } from "../context/WebSocketContext";

export default function useChatSocket({ roomId, onMessage }) {
    const { subscribe, send } = useWebSocket();
    const onMessageRef = useRef(onMessage);

    useEffect(() => { onMessageRef.current = onMessage; }, [onMessage]);
    
    useEffect(() => {
       if(!roomId) return;
       // 공유 연결에 구독만 추가
       const unsubscribe = subscribe(
        `/topic/chat/room/${roomId}`,
        (msg) => onMessageRef.current(msg)
       );
       return unsubscribe;
    },[roomId, subscribe]);

    const sendMessage = useCallback((content) => {
       send(`/app/chat/room/${roomId}`, { content });
    }, [roomId, send]);

    return { sendMessage };
}