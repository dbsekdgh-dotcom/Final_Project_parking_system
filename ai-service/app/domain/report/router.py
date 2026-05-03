from fastapi import APIRouter, UploadFile, File, HTTPException
from fastapi.responses import Response
from pydantic import BaseModel
from typing import Any
from app.domain.entry.entryimagesave.entryimagesaveservice import upload_file_to_s3
from app.domain.report.report_service import generate_report_comment
from app.domain.report.file_generator import generate_excel_report
import io

report_router = APIRouter()

class ReportRequest(BaseModel):
    data: dict[str, Any]
    period: str          # "MONTHLY" | "WEEKLY"
    start_date: str      # "2025-04-01"
    end_date: str        # "2025-04-30"

@report_router.post("/generate")
async def generate_report(req: ReportRequest):
    try:
        comments = generate_report_comment(req.data, req.period)
        excel_bytes = generate_excel_report(
            req.data, comments, req.period, req.start_date, req.end_date
        )
        period_label = "monthly" if req.period == "MONTHLY" else "weekly"
        filename = f"parking_report_{period_label}_{req.start_date}.xlsx"
        return Response(
            content=excel_bytes,
            media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            headers={"Content-Disposition": f"attachment; filename={filename}"}
        )
    except Exception as e:
        print(f"보고서 생성 오류: {e}")
        raise HTTPException(status_code=500, detail=str(e))

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