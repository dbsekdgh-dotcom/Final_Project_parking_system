import numpy as np
import cv2
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.domain.entryexitocr.router import entryexit_router
from app.domain.payment.router import payment_router

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(
    entryexit_router,
    prefix="/api/v1/parking/entryexit",
    tags=["EntryExit OCR"]
)

app.include_router(
    payment_router,
    prefix="api/v1/parking/payment"
)

if __name__ == "__main__":
    import uvicorn
    # 도커 환경에서 외부 접속을 허용하려면 host를 "0.0.0.0"으로 잡아야 합니다.
    uvicorn.run(app, host="0.0.0.0", port=8000)