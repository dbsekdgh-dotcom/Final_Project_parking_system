
from fastapi import APIRouter, HTTPException
from app.domain.kioskChatbot.kiosk_chatbot import chat
from app.domain.kioskChatbot.kiosk_redis import clear_messages
from pydantic import BaseModel

class ChatRequest(BaseModel):
    session_id: str
    user_question: str
    screen_id: str = None

kiosk_chatbot_router=APIRouter()

@kiosk_chatbot_router.post("/chat")
async def kiosk_chat(data:ChatRequest):
    try:
        answer=chat(data.session_id,data.user_question,data.screen_id)
        return {"answer":answer}
    except Exception as e:
        raise HTTPException(status_code=500, detail="챗봇 답변 중 에러가 발생했습니다.")

@kiosk_chatbot_router.delete("/end")
async def kiosk_end_chat(session_id: str):
    try:
        clear_messages(session_id)
        return {"ok": True}
    except Exception as e:
        raise HTTPException(status_code=500, detail="챗봇 기록 삭제 중 에러가 발생했습니다.")