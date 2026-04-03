import numpy as np
import cv2
import os
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.domain.entryexitocr.router import entryexit_router
from app.domain.entry.entryimagesave.entryimagesaveRouter import s3_router
app = FastAPI()

print("AWS_REGION =", os.getenv("AWS_REGION"))
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
    s3_router,
    prefix="/api/v1/s3",
    tags=["S3 Upload"]
)

