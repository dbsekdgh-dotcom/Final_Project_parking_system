from fastapi import APIRouter, UploadFile, File
from app.domain.entry.entryimagesave.entryimagesaveservice import upload_file_to_s3

s3_router = APIRouter()

@s3_router.post("/upload")
async def upload_image(file: UploadFile = File(...)):
    file_url = upload_file_to_s3(file)
    
    return {
        "message": "upload success",
        "fileUrl": file_url
    }