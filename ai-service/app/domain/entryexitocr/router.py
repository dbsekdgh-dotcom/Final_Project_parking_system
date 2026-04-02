from fastapi import APIRouter, UploadFile, File
from .OCRService import extract_plate_number

entryexit_router = APIRouter()

@entryexit_router.post("/plate-ocr")
async def plate_ocr(file: UploadFile=File(...)):
    
    plate = await extract_plate_number(file)
    return {"plateNumber": plate}