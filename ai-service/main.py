import os
from dotenv import load_dotenv

# app 모듈 import 전에 .env 로드 (s3_client 등 모듈 레벨 초기화에 필요)
load_dotenv(dotenv_path=os.path.join(os.path.dirname(__file__), "../.env"))

import numpy as np
import cv2
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.domain.entryexitocr.router import entryexit_router
from app.domain.payment.router import payment_router
from app.domain.report.router import report_router
from app.domain.userchatbot.router import router as chatbot_router
from app.domain.notification.router import router as notification_router

app = FastAPI()

print("AWS_REGION=", os.getenv("AWS_REGION"))
print("S3_BUCKET_NAME=", os.getenv("S3_BUCKET_NAME"))

CORS_ORIGINS = os.getenv("CHATBOT_CORS_ORIGIN", "http://localhost:5202").split(",")

app.add_middleware(
    CORSMiddleware,
    allow_origins=CORS_ORIGINS,
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
    prefix="/api/v1/parking/payment"
)

app.include_router(
    report_router,
    prefix="/api/v1/parking/report",
    tags=["Report"]
)

app.include_router(
    chatbot_router,
    prefix="/api/v1/parking",
    tags=["Chatbot"]
)

app.include_router(
    notification_router,
    prefix="/api/v1/notification",
    tags=["Notification"]
)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
