
import uvicorn

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.core.entry.config import settings
from app.api.entry.router import router

app = FastAPI(title=settings.PROJECT_NAME)

# 프론트(브라우저)에서 다른 포트로 OCR API 호출할 때 발생하는 CORS 문제를 피하기 위해 허용합니다.
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(router, prefix=settings.API_V1_STR)
@app.get("/")
def root():
    return {"message": "주차 관리 AI 서비스가 정상 작동 중입니다."}

if __name__=="__main__":
    uvicorn.run("main:app",host="0.0.0.0",port=8000, reload=True)