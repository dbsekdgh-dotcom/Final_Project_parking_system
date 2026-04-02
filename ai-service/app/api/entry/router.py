from fastapi import APIRouter, UploadFile, File, HTTPException
import os
import uuid
import boto3  # S3 사용을 위해 필요
from app.services.entry.ocr_service import ocr_service  # 위에서 만든 OCRService 임포트

router = APIRouter(prefix="/parking", tags=["Parking"])

# S3 설정 (환경변수 권장)
S3_BUCKET = os.getenv("S3_BUCKET_NAME")
s3_client = boto3.client('s3')

@router.post("/entryexit")
async def vehicle_entry(file: UploadFile = File(...)):
    """
    차량 입차 시 이미지를 받아 S3에 저장하고 번호판을 분석합니다.
    """
    # 1. 파일 확장자 체크
    extension = file.filename.split(".")[-1].lower()
    if extension not in ["jpg", "jpeg", "png"]:
        raise HTTPException(status_code=400, detail="지원하지 않는 이미지 형식입니다.")

    # 2. S3에 저장할 고유 파일명 생성
    file_name = f"entry/{uuid.uuid4()}.{extension}"

    try:
        # 3. S3 업로드
        # file.file을 직접 읽어 S3에 전송합니다.
        s3_client.upload_fileobj(
            file.file,
            S3_BUCKET,
            file_name,
            ExtraArgs={'ContentType': file.content_type}
        )

        # 4. S3 객체 URL 생성 (Public 읽기가 가능한 권한 설정이 필요합니다)
        image_url = f"https://{S3_BUCKET}.s3.amazonaws.com/{file_name}"

        # 5. [핵심] OCR 서비스 호출 (기존의 보정 로직이 포함된 함수)
        # API 분석 + normalize + 정규식 검증이 한 번에 일어납니다.
        detected_plate = ocr_service.extract_license_plate_from_url(image_url)

        # 6. 최종 결과 반환
        return {
            "success": True,
            "plate_number": detected_plate,
            "image_url": image_url,
            "message": "입차 처리가 완료되었습니다."
        }

    except Exception as e:
        print(f" 라우터 에러: {str(e)}")
        raise HTTPException(status_code=500, detail="서버 내부 오류가 발생했습니다.")