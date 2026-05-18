import { createContext, useCallback, useContext, useEffect, useRef } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

const WebSocketContext = createContext(null);

export function WebSocketProvider({ children }) {
    const clientRef = useRef(null);
    const pendingRef = useRef([]); // 연결전 subscribe 요청 임시 보관

    useEffect(() => {
        const token = sessionStorage.getItem('accessToken');
        if(!token) return;

        const client = new Client({
            webSocketFactory: () => new SockJS('/ws/admin'),
            connectHeaders: { Authorization: `Bearer ${token}`},
            onConnect: () =>{
                // 연결전에 쌓인 구독 요청 일괄처리
                pendingRef.current.forEach(({ destination, callback, resolve }) =>{
                    const sub = client.subscribe(destination, (frame) =>{
                        callback(JSON.parse(frame.body));
                    });
                    resolve(sub);
                });
                pendingRef.current = [];
            },
            reconnectDelay: 5000,
        });
        client.activate();
        clientRef.current = client;

        return () => client.deactivate();
    }, []);

    const subscribe = useCallback((destination, callback) => {
        const client = clientRef.current;

        // 이미 연결됐으면 바로 구독
        if (client?.connected){
            const sub = client.subscribe(destination, (frame) =>{
                callback(JSON.parse(frame.body));
            });
            return () => sub.unsubscribe();
        }

        // 연결전이면 pending 큐에 넣고, 나중에 sub받아서 unsubscribe 가능 하게 처리
        let sub = null;
        let cancelled = false;

        const promise = new Promise((resolve) => {
            pendingRef.current.push({ destination, callback, resolve });
        });
        promise.then((s) =>{
            sub = s;
            if(cancelled) sub.unsubscribe();
        });

        return () =>{
            cancelled = true;
            sub?.unsubscribe();
            pendingRef.current = pendingRef.current.filter(
                (p) => p.destination !== destination
            );
        };
    }, []);

    const send = useCallback((destination, body) => {
        const client = clientRef.current;
        if(client?.connected){
            client.publish({ destination, body: JSON.stringify(body) });
        }
    },[]);

    return (
        <WebSocketContext.Provider value={{subscribe,send}}>
            {children}
        </WebSocketContext.Provider>
    );
}

export const useWebSocket = () => useContext(WebSocketContext);