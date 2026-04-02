import numpy as np
import cv2
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.domain.entryexitocr.router import entryexit_router

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
    prefix="/api/v1/entryexit",
    tags=["EntryExit OCR"]
)
