import boto3
import uuid
from datetime import datetime

s3_client=boto3.client(
    "s3",
    aws_access_key_id="AWS_ACCESS_KEY_ID",
    aws_secret_access_key="AWS_SECRET_ACCESS_KEY",
    region_name="S3_REGION"
)
BUCKET_NAME="S3_BUCKET_NAME"

def upload_file_to_s3(file)-> str:
    filename =f"{datetime.now().strftime('%Y%m%d_%H%M%s')}_{uuid.uuid4().hex}.jpg"
    s3_client.upload_fileobj(file.file, BUCKET_NAME, filename)
    return f"https://{BUCKET_NAME}.s3.{s3_client.meta.region_name}.amazonaws.com/{filename}"
