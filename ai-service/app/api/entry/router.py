from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from app.services.entry.ocr_service import ocr_service

api_router = APIRouter()

class OCRRequest(BaseModel):
    image_url: str


@api_router.post("/entry/plate-ocr")
async def plate_ocr(request: OCRRequest):
    if not request.image_url:
        raise HTTPException(status_code=400, detail="S3 이미지 URL이 누락되었습니다.")
        
    try:
        plate_number = ocr_service.extract_license_plate_from_url(request.image_url)

        return {
            "status":"success",
            "plateNumber": plate_number
        }

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"OCR 분석 중 서버 오류: {str(e)}")

    
