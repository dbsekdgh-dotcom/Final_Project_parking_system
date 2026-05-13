import { useCallback, useEffect, useRef } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "@sockjs-client";

export default function useChatSocket({ roomId, onMessage }) {
    const clientRef = useRef(null);
    const onMessageRef = useRef(onMessage);

    useEffect(() => { onMessageRef.current = onMessage; }, [onMessage]);
    
    const sendMessage = useCallback((content) => {
        if(clientRef.current?.connected){
            clientRef.current.publish({
                destination: `/app/chat/room/${roomId}`,
                body: JSON.stringify({ content }),
            });
        }
    }, [roomId]);

    useEffect(() => {
        if (!roomId) return;

        const token = sessionStorage.getItem('accessToken');
        const client = new Client({
            webSocketFactory: ()=> new SockJS('/ws/admin'),
            connectHeaders: { Authorization: `Bearer ${token}` },
            onConnect: ()=>{
                client.subscribe(`/topic/chat/room/${roomId}`, (frame)=>{
                    onMessageRef.current(JSON.parse(frame.body));
                });
            },
            reconnectDelay: 5000,
        });
        client.activate();
        clientRef.current = client;

        return () => client.deactivate();
    },[roomId]);

    return { sendMessage };
}