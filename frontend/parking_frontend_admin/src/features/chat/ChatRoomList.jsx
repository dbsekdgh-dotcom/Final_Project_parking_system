import { useEffect, useRef, useState } from 'react'
import { createPortal } from 'react-dom'
import { getMyChatRooms, leaveGroupRoom } from '../../shared/api/chatApi'
import './ChatRoomList.css'

export default function ChatRoomList({ onClose, position, excludeRef, onOpenChat, onLeave }) {
    const [rooms, setRooms] = useState([])
    const [loading, setLoading] = useState(true)
    const popoverRef = useRef(null)

    useEffect(() => {
        getMyChatRooms()
            .then(res => setRooms(res.data))
            .catch(console.error)
            .finally(() => setLoading(false))
    }, [])

    useEffect(() => {
        const handler = (e) => {
            if (
                popoverRef.current && !popoverRef.current.contains(e.target) &&
                excludeRef.current && !excludeRef.current.contains(e.target)
            ) {
                onClose()
            }
        }
        document.addEventListener('mousedown', handler)
        return () => document.removeEventListener('mousedown', handler)
    }, [onClose, excludeRef])

    const handleOpenRoom = (room) => {
        onOpenChat({
            roomId: room.roomId,
            roomName: room.roomName,
            members: room.members,
            roomType: room.roomType,
        })
        onClose()
    }

    const handleLeave = async (e, roomId) => {
        e.stopPropagation()
        if (!window.confirm('채팅방에서 나가시겠습니까?')) return
        try {
            await leaveGroupRoom(roomId)
            setRooms(prev => prev.filter(r => r.roomId !== roomId))
            if (onLeave) onLeave(roomId)
        } catch (err) {
            console.error(err)
        }
    }

    const totalUnread = rooms.reduce((sum, r) => sum + (r.unreadCount || 0), 0)

    return createPortal(
        <div ref={popoverRef} className="chatroom-list" style={{ left: position.left, bottom: position.bottom }}>
            <div className="chatroom-list__header">
                <span>
                    채팅방
                    {totalUnread > 0 && (
                        <span className="chatroom-list__total-badge">{totalUnread}</span>
                    )}
                </span>
                <button className="chatroom-list__close" onClick={onClose}>&#10005;</button>
            </div>
            <div className="chatroom-list__body">
                {loading ? (
                    <p className="chatroom-list__empty">불러오는 중...</p>
                ) : rooms.length === 0 ? (
                    <p className="chatroom-list__empty">참여 중인 채팅방이 없습니다.</p>
                ) : (
                    rooms.map(room => (
                        <div
                            key={room.roomId}
                            className="chatroom-list__item"
                            onClick={() => handleOpenRoom(room)}
                        >
                            <div className="chatroom-list__avatar">
                                {room.roomName.charAt(0)}
                            </div>
                            <div className="chatroom-list__info">
                                <span className="chatroom-list__name">{room.roomName}</span>
                                {room.lastMessage && (
                                    <span className="chatroom-list__last">{room.lastMessage}</span>
                                )}
                            </div>
                            <div className="chatroom-list__right">
                                {room.unreadCount > 0 && (
                                    <span className="chatroom-list__unread">{room.unreadCount}</span>
                                )}
                                {room.roomType === 'GROUP' && (
                                    <button
                                        className="chatroom-list__leave"
                                        onClick={(e) => handleLeave(e, room.roomId)}
                                    >
                                        나가기
                                    </button>
                                )}
                            </div>
                        </div>
                    ))
                )}
            </div>
        </div>,
        document.body
    )
}
