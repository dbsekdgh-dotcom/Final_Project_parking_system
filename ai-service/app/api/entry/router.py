import os
import requests
import io
from dotenv import load_dotenv

# 최상위 .env 로드 (아까 성공한 경로 설정 유지)
parent_dir = os.path.abspath(os.path.join(os.getcwd(), ".."))
env_path = os.path.join(parent_dir, ".env")
load_dotenv(dotenv_path=env_path)

class OCRService:
    def extract_license_plate_from_url(self, image_url: str):
        """
        [실전용] S3 URL을 받아 해당 이미지를 분석합니다.
        """
        API_TOKEN = os.getenv("PLATE_RECOGNIZER_TOKEN")
        
        if not API_TOKEN:
            return "설정 오류 (토큰 없음)"

        print(f"--- [Plate Recognizer] 실전 분석 시작 ---")
        print(f"📸 분석할 S3 주소: {image_url}")

        try:
            # 1. S3 URL에서 이미지 데이터 가져오기 (메모리에 임시 저장)
            response = requests.get(image_url, timeout=10)
            if response.status_code != 200:
                return f"이미지 다운로드 실패 ({response.status_code})"
            
            # 이미지 바이트 데이터를 파일처럼 취급할 수 있게 변환
            image_bytes = io.BytesIO(response.content)

            # 2. Plate Recognizer API에 전송 (파일 대신 메모리 데이터 전송)
            api_response = requests.post(
                "https://api.platerecognizer.com/v1/plate-reader/",
                data=dict(regions=["kr"]),
                files=dict(upload=image_bytes), # test.jpg 대신 다운로드한 데이터 사용
                headers={"Authorization": f"Token {API_TOKEN}"}
            )

            # 3. 결과 처리
            if api_response.status_code in [200, 201]:
                result_data = api_response.json()
                if result_data.get("results"):
                    detected_plate = result_data["results"][0]["plate"]
                    print(f"👉 최종 인식된 번호: {detected_plate}")
                    return detected_plate.upper()
                return "인식 실패"
            else:
                return f"API 에러 ({api_response.status_code})"

        except Exception as e:
            print(f"!!! 시스템 에러 발생 !!!: {str(e)}")
            return f"에러: {str(e)}"

ocr_service = OCRService()