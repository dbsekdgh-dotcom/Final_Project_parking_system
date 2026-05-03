import boto3
import uuid
from datetime import datetime
from zoneinfo import ZoneInfo
import os

s3_client=boto3.client(
    "s3",
    aws_access_key_id=os.getenv("AWS_ACCESS_KEY_ID"),
    aws_secret_access_key=os.getenv("AWS_SECRET_ACCESS_KEY"),
    region_name=os.getenv("S3_REGION")
)
BUCKET_NAME=os.getenv("S3_BUCKET_NAME")

def upload_file_to_s3(file)-> str:
    filename =f"{datetime.now(ZoneInfo('Asia/Seoul')).strftime('%Y%m%d_%H%M%S')}_{uuid.uuid4().hex}.jpg"
    s3_client.upload_fileobj(file.file,
                             BUCKET_NAME,
                             filename,
                             ExtraArgs={"ContentType": "image/jpeg"}
                             )
    return f"https://{BUCKET_NAME}.s3.{s3_client.meta.region_name}.amazonaws.com/{filename}"
