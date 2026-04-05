import redis
from fastapi import APIRouter
from app.domain.payment import Lock_service
from pydantic import BaseModel

class PaymentRequest(BaseModel):
    car_number:str

payment_router=APIRouter()

@payment_router.post("/payment-start")
async def start_payment(data:PaymentRequest):
    car_number=data.car_number
    # 락 시도 (이미 락 걸린 차량번호는 서비스에서 에러 409 응답)
    Lock_service.aquire_lock(car_number)
    # 성공 시 응답
    return {
        "status":"SUCCESS",
        "car_number":car_number,
        "message": "락 획득 완료"
    }
    
@payment_router.post("/payment-end")
async def end_payment(data:PaymentRequest):
    car_number=data.car_number
    Lock_service.lock_release(car_number)
    return {
        "status":"SUCCESS",
        "car_number":car_number,
        "message": "락 해제 완료"
    }