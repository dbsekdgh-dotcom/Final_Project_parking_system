import os
import requests
from dotenv import load_dotenv

# [핵심] 현재 작업 디렉토리의 부모 폴더(..)에 있는 .env 파일을 찾아 로드합니다.
parent_dir = os.path.abspath(os.path.join(os.getcwd(), ".."))
env_path = os.path.join(parent_dir, ".env")

# 해당 경로에 파일이 있는지 확인하고 로드
if os.path.exists(env_path):
    load_dotenv(dotenv_path=env_path)
    print(f"✅ 최상위 .env 로드 성공: {env_path}")
else:
    print(f"❌ 최상위 .env를 찾지 못했습니다: {env_path}")

class OCRService:
    def extract_license_plate_from_url(self, image_url: str):
        # .env에서 값을 읽어옵니다.
        API_TOKEN = os.getenv("PLATE_RECOGNIZER_TOKEN")
        
        if not API_TOKEN:
            print("❌ [경고] API_TOKEN이 여전히 None입니다. .env 파일 내용을 확인하세요.")
            return "설정 오류 (토큰 없음)"
            
        temp_filename = "test.jpg"
        temp_path = os.path.abspath(os.path.join(os.getcwd(), temp_filename))

        print(f"--- [Plate Recognizer] 분석 시작 (Token 확인됨) ---")

        try:
            if not os.path.exists(temp_path):
                return "파일 없음"

            with open(temp_path, "rb") as fp:
                response = requests.post(
                    "https://api.platerecognizer.com/v1/plate-reader/",
                    data=dict(regions=["kr"]),
                    files=dict(upload=fp),
                    headers={"Authorization": f"Token {API_TOKEN}"}
                )

            if response.status_code in [200, 201]:
                result_data = response.json()
                if result_data.get("results"):
                    detected_plate = result_data["results"][0]["plate"]
                    print(f"👉 인식 결과: {detected_plate}")
                    return detected_plate.upper()
                return "인식 실패"
            else:
                print(f"❌ API 에러: {response.status_code} - {response.text}")
                return f"API 에러 ({response.status_code})"

        except Exception as e:
            return f"에러: {str(e)}"

ocr_service = OCRService()