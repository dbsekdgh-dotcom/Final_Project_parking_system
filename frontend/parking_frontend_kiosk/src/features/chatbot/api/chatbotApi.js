import kioskApi from '../../../shared/api/kioskApi';

export const sendChat = async(sessionId, userQuestion, screenId) =>{
    const res=await kioskApi.post('/api/kiosk/chatbot/chat', {
        sessionId,
        userQuestion,
        screenId: screenId || '',
    })
    return res.data
};

export const endChat = (sessionId) =>
    kioskApi.delete(`/api/kiosk/chatbot/end`,{
        params:{sessionId}
    });
