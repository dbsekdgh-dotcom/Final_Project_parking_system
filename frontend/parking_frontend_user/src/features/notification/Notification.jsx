import React, { useRef, useEffect, useState} from "react";
import ReactDOM from "react-dom";
import { fetchNotifications, markAsRead, deleteNotification } from "./api/NotificationApi";
import './Notification.css';
import axios from "axios";
import { useNavigate } from "react-router-dom";

const Noti = ({ onMutationSuccess, onClose}) =>{
    const navigate = useNavigate();
    const [selectedNoti, setSelectedNoti] =useState(null); //

    const [notifications, setNotifications] = useState([]);
    const [loading, setLoading] = useState(true);
    const dropdownRef = useRef(null);

   
    // 알림 데이터 가져오기
    useEffect(() => {
        fetchNotifications()
        .then((data) => {
            const sortedData = data.sort((a, b) => {
                const aRead = a.readAt ? 1 : 0;
                const bRead = b.readAt ? 1 : 0;

                if (aRead !== bRead) {
                    return aRead - bRead; 
                }
                return new Date(b.createdAt) - new Date(a.createdAt);
            });

            setNotifications(sortedData);
            setLoading(false);
        })
        .catch((err) => { 
            console.error("알림을 가져오는 데 실패했어요!", err);
            setLoading(false);
        });
    }, []);
   
    
//알림을 클릭하면 읽음을 처리하는 함수
const handleRead =(noti) =>{
    const id = noti.notificationId;
    markAsRead(id).then(()=>{
        setNotifications(notifications.map(n=>
            n.notificationId ===id ? { ...n, readAt: new Date() } : n
        ));
        if (onMutationSuccess) onMutationSuccess();
    });
    setSelectedNoti(noti);
};

const handleDelete = async (id, e) => {
    e.stopPropagation();
    if (!window.confirm("이 알림을 삭제하시겠습니까?")) return;

    const baseUrl = import.meta.env.VITE_API_BASE_URL;

    try{
        await axios.delete(`${baseUrl}/api/user/notifications/${id}`,{
                withCredentials: true
            });

        setNotifications(notifications.filter(noti => noti.notificationId !== id));
        if (onMutationSuccess) onMutationSuccess();
        }catch (error){
            console.error("삭제 실패:", error);
            alert("삭제 처리 중 오류가 발생했습니다.");            
        }      
    };

if (loading) return <div className="p-4 text-sm text-gray-500">로딩 중...</div>;

return (
    <>
        {/*  투명 배경막 (부모 Sidebar 상태를 끔) */}
        <div 
            className="noti-overlay-trigger"
            onClick={onClose} 
            style={{
                position: 'fixed',
                top: 0, left: 0, width: '100vw', height: '100vh',
                background: 'transparent',
                zIndex: 9999
            }}
        /> 

        {/*  알림 드롭다운 */}
        <div className="noti-dropdown" style={{ zIndex: 10000 }}>
            <div className="noti-header">
                <span>최신 알림</span>
                {notifications.length > 0 && (
                    <span className="noti-count">{notifications.length}</span>
                )}
            </div>
            
            <ul className="noti-list">
                {notifications.length === 0 ? (
                    <li className="no-notifications">새로운 알림이 없어요.</li>
                ) : (
                    notifications.map((noti) => (
                        <li 
                            key={noti.notificationId}
                            onClick={() => handleRead(noti)}
                            className={`noti-item ${!noti.readAt ? 'unread' : 'read'}`}
                        >
                            <div className="noti-content-area">
                                <div className="noti-message">{noti.content}</div>
                                <div className="noti-date">
                                    {noti.createdAt ? new Date(noti.createdAt).toLocaleString('ko-KR') : '날짜 정보 없음'}
                                </div>
                            </div>
                            <button 
                                className="delete-noti-btn"
                                onClick={(e) => handleDelete(noti.notificationId, e)}
                            > 삭제 </button>
                        </li>
                    ))
                )}
            </ul>

            <div className="noti-footer">
                <span>모든 알림은 위 리스트에서 확인 가능합니다.</span>
            </div>
        </div>

            {/*  상세 보기 모달 — transform 컨테이너 밖으로 포털 */}
            {selectedNoti && ReactDOM.createPortal(
                <div className="noti-modal-overlay" onClick={() => setSelectedNoti(null)}>
                    <div className="noti-modal-content" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h3>알림 상세 내용</h3>
                            <button className="close-btn" onClick={() => setSelectedNoti(null)}>×</button>
                        </div>
                        <div className="modal-body">
                            <p className="modal-text">{selectedNoti.content}</p>
                            <span className="modal-time">
                                {new Date(selectedNoti.createdAt).toLocaleString('ko-KR')}
                            </span>
                        </div>
                    </div>
                </div>,
                document.body
            )}
        </>
    ); 
}; 

export default Noti;
