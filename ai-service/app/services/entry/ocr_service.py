import os
import requests
import uuid
import subprocess

class OCRService:
    def extract_license_plate_from_url(self, image_url: str):

        temp_filename = f"temp_{uuid.uuid4()}.jpg"

        temp_path = os.path.join(os.getcwd(), temp_filename)

        try:
            response = requests.get(image_url)

            if response.status_code != 200:
                return "S3 이미지 다운로드 실패"

            with open(temp_path, "wb") as f:
                f.write(response.content)
            
            image_dir = os.path.dirname(temp_path)
            command = [
                "docker", "run", "--rm",
                "-v", f"{image_dir}:/data",
                "openalpr/openalpr",
                "-c", "kr",
                f"/data/{temp_filename}"
            ]
            # 명령어 실행 후 터미널에 찍히는 글자(stdout)를 낚아챕니다.
            result = subprocess.check_output(command, stderr=subprocess.STDOUT)
            return result.decode("utf-8") # 바이트 데이터를 사람이 읽는 글자로 변환
            

        finally:
            if os.path.exists(temp_path):
                os.remove(temp_path)

ocr_service = OCRService()