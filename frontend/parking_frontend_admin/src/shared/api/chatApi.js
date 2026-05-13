import adminApi from "./adminApi";

export const getOrCreateDirectRoom = (targetAdminId) =>
    adminApi.post('/chat/rooms/direct',{ targetAdminId });

export const createGroupRoom = (name, memberIds) =>
    adminApi.post('/chat/rooms/group', { roomName: name, memberAdminIds: memberIds });

export const getMyChatRooms = () =>
    adminApi.get('/chat/rooms');

export const getChatMessages = (roomId, page = 0, size = 50) =>
    adminApi.get(`/chat/rooms/${roomId}/messages`,{ params: { page, size } });

export const markAsRead = (roomId) =>
    adminApi.post(`/chat/rooms/${roomId}/read`);

export const leaveGroupRoom = (roomId) =>
    adminApi.delete(`/chat/rooms/${roomId}/leave`);