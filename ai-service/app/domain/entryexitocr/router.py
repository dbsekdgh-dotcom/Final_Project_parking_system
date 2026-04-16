from fastapi import APIRouter, UploadFile, File
from .OCRService import extract_plate_number
from app.domain.entry.entryimagesave.entryimagesaveservice import upload_file_to_s3
import io

entryexit_router = APIRouter()

@entryexit_router.post("/")
async def plate_ocr(file: UploadFile = File(...)):
    # 파일을 메모리에 읽어두기 (OCR과 S3 업로드에 각각 사용)
    file_bytes = await file.read()

    # OCR: 번호판 인식
    ocr_file = UploadFile(filename=file.filename, file=io.BytesIO(file_bytes))
    plate = await extract_plate_number(ocr_file)

    # S3 업로드
    s3_file = UploadFile(filename=file.filename, file=io.BytesIO(file_bytes))
    s3_path = upload_file_to_s3(s3_file)

    return {"plateNumber": plate, "s3path": s3_path}


