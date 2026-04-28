import api from '../../auth/api/axios';

// 모든 알림 목록 가져오기
export const fetchNotifications = () => 
    api.get('/api/user/notifications').then(r => r.data);

// 특정 알림 읽음 표시하기
export const markAsRead = (notificationId) =>
    api.patch(`/api/user/notifications/${notificationId}/read`).then(r =>r.data);


// 특정 알림 삭제하기
export const deleteNotification = (notificationId) =>
    api.delete(`/api/user/notifications/${notificationId}`).then(r => r.data);

export const fetchUnreadCount = async () => {
    try{
        const response = await api.get('/api/user/notifications/unread-count');
        return response.data;
    }catch (error){
        console.error("안 읽은 알림 개수 조회 실패:", error);
        return 0;
    }
};