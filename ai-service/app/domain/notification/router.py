from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from .graph import notification_graph

router = APIRouter()

class NotificationRequest(BaseModel):
    notification_type: str
    context: dict

class NotificationResponse(BaseModel):
    title:str
    content:str

@router.post("/generate",response_model=NotificationResponse)
async def generate_notification(req:NotificationRequest):
    if req.notification_type not in[
        "VEHICLE_APPROVED", "VEHICLE_REJECTED", "RESIDENT_APPROVED",
        "RESERVATION_APPROVED", "RESERVATION_REJECTED"
    ]:
        raise HTTPException(status_code=400, detail=f"알 수 없는 타입:{req.notification_type}")
    state = await notification_graph.ainvoke({
        "notification_type": req.notification_type,
        "context": req.context,
        "title": "",
        "content": "",
    })
    return NotificationResponse(title=state["title"], content=state["content"])