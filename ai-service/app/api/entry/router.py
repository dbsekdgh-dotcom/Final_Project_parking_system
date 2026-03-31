import os
import tempfile

from fastapi import APIRouter, File, HTTPException, UploadFile

from app.services.entry.ocr_service import ocr_service

api_router = APIRouter()


@api_router.get("/test")
def test_connection():
    return {
        "status": "success",
        "message": "안내판(API Router)이 메인 스위치와 잘 연결되었습니다!"
    }


@api_router.post("/entry/plate-ocr")
async def plate_ocr(file: UploadFile = File(...)):
    if not file:
        raise HTTPException(status_code=400, detail="파일이 비어 있습니다.")

    suffix = os.path.splitext(file.filename or "")[1] or ".jpg"
    try:
        contents = await file.read()
    except Exception:
        raise HTTPException(status_code=400, detail="파일 읽기 실패")

    with tempfile.TemporaryDirectory() as tmpdir:
        image_path = os.path.join(tmpdir, f"upload{suffix}")
        with open(image_path, "wb") as f:
            f.write(contents)

        try:
            plate_number = ocr_service.extract_license_plate(image_path)
        except Exception as e:
            raise HTTPException(status_code=500, detail=f"OCR 처리 실패: {str(e)}")

    return {"plateNumber": plate_number}