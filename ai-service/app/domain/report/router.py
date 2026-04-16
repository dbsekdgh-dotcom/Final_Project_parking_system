from fastapi import APIRouter, UploadFile, File
from app.domain.entry.entryimagesave.entryimagesaveservice import upload_file_to_s3
import io

report_router = APIRouter()
@report_router.post("/") # 리액트에서 매핑경로를 연결 리액트 -> 파이썬
async def report_s3path(file: UploadFile =File(...)):
    try:
        file_bytes = await file.read()
        # S3 업로드
        s3_file = UploadFile(filename=file.filename, file=io.BytesIO(file_bytes))
        s3_path = upload_file_to_s3(s3_file)
        
        return {"report_s3path": s3_path}
        # 리턴되는 s3_path를 "report_s3path" 로 받기   
        # 리액트에서 받은 report_s3path를 데이터들과 묶어서 BackEnd 매핑주소로 보내기
    except Exception as e:
        print(f"파이썬 내부 에러 발생: {e}")
        return {"error": str(e)}, 500