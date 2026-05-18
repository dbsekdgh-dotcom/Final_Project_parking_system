import { useEffect, useRef, useState, useCallback} from "react";
import { getChatMessages, markAsRead, leaveGroupRoom } from "../../shared/api/chatApi";
import useChatSocket from "../../shared/hooks/useChatSocket";
import './ChatWindow.css';


export default function ChatWindow({ roomId, roomName, myAdminId, members = [], roomType, index, onClose, onLeave }){
    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState('');
    const [showMembers, setShowMembers] = useState(false);
    const bottomRef = useRef(null);

    useEffect(()=>{
        getChatMessages(roomId)
            .then(res => {
                setMessages([...res.data].reverse());
            })
            .catch(console.error);
        markAsRead(roomId).catch(console.error);

        return () => {
          markAsRead(roomId).catch(console.error);  // 닫을 때
      };
    }, [roomId]);

    useEffect(()=>{
        bottomRef.current?.scrollIntoView({ behavior: 'smooth'});
    }, [messages]);

    const handleNewMessages = useCallback((msg)=> {
        setMessages(prev => [...prev, msg]);
        markAsRead(roomId).catch(console.error);
    }, [roomId]);

    const { sendMessage } = useChatSocket({ roomId, onMessage: handleNewMessages});

    const handleSend = () =>{
        const trimmed = input.trim();
        if(!trimmed) return;
        sendMessage(trimmed);
        setInput('');
    };

    const handleKeyDown = (e) =>{
        if(e.key === 'Enter' && !e.shiftKey){
            e.preventDefault();
            handleSend();
        }
    };

    const handleLeave = async () => {
        if (!window.confirm('채팅방에서 나가시겠습니까?')) return
        try {
            await leaveGroupRoom(roomId)
            onLeave()
        } catch (err) {
            console.error(err)
        }
    }

    const rightOffset = 16 + index * 336;

    return (
          <div className="chat-window" style={{ right: rightOffset }}>
              <div className="chat-window__header">
                  <span className="chat-window__title">{roomName}</span>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                      {members.length > 0 && (
                          <button
                              className="chat-window__members-btn"
                              onClick={() => setShowMembers(p => !p)}
                              title="참여자 목록"
                          >
                              &#128101; {members.length}
                          </button>
                      )}
                      {roomType === 'GROUP' && (
                          <button
                              className="chat-window__leave-btn"
                              onClick={handleLeave}
                              title="채팅방 나가기"
                          >
                              나가기
                          </button>
                      )}
                      <button className="chat-window__close" onClick={onClose}>&#10005;</button>
                  </div>
              </div>

              {showMembers && (
                  <div className="chat-window__member-list">
                      {members.map(m => (
                          <div key={m.adminId} className="chat-window__member-item">
                              <div className="chat-window__member-avatar">
                                  {m.name.charAt(0)}
                              </div>
                              <span className="chat-window__member-name">
                                  {m.name}
                                  {m.adminId === myAdminId && (
                                      <span className="chat-window__member-me"> (나)</span>
                                  )}
                              </span>
                          </div>
                      ))}
                  </div>
              )}

              <div className="chat-window__messages">
                  {messages.map(msg => (
                      <div
                          key={msg.messageId}
                          className={`chat-msg ${msg.senderId === myAdminId ? 'chat-msg--mine' : 'chat-msg--theirs'}`}
                      >
                          {msg.senderId !== myAdminId && (
                              <span className="chat-msg__sender">{msg.senderName}</span>
                          )}
                          <div className="chat-msg__bubble">{msg.content}</div>
                          <span className="chat-msg__time">
                              {new Date(msg.createdAt).toLocaleTimeString('ko-KR', {
                                  hour: '2-digit', minute: '2-digit'
                              })}
                          </span>
                      </div>
                  ))}
                  <div ref={bottomRef} />
              </div>

              <div className="chat-window__input-row">
                  <textarea
                      className="chat-window__input"
                      value={input}
                      onChange={e => setInput(e.target.value)}
                      onKeyDown={handleKeyDown}
                      placeholder="메시지 입력 (Enter 전송)"
                      rows={1}
                  />
                  <button className="chat-window__send" onClick={handleSend}>전송</button>
              </div>
          </div>
      );
}
