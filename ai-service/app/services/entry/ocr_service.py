import os
import io
import re
import json
import requests
from dotenv import load_dotenv

load_dotenv()

class OCRService:
    def __init__(self):
        self.api_token = os.getenv("PLATE_RECOGNIZER_TOKEN")
        self.api_url = "https://api.platerecognizer.com/v1/plate-reader/"

    def normalize_plate(self, plate_text: str) -> str:
        """문자열 보정: 숫자만 7~8자리인 경우 한글 위치에 '러'나 '가' 등을 강제 삽입 및 오타 수정"""
        if not plate_text or not isinstance(plate_text, str): 
            return ""
        
        # 1. 특수문자 제거 및 대문자화
        plate = re.sub(r"[^0-9A-Z가-힣]", "", plate_text.upper())
        # 영어 O를 숫자 0으로 변환 (자주 발생하는 오차)
        plate = plate.replace("O", "0")

        # 2. 한국 번호판 특수 보정: 숫자만 7자리인 경우 (예: 1547070 -> 154러7070)
        # API가 한글을 아예 못 읽고 숫자로 치환했을 때를 대비합니다.
        if plate.isdigit():
            if len(plate) == 7:
                # 3자리 + 한글(러) + 4자리 형태일 가능성이 매우 높음 (최신 번호판)
                plate = plate[:3] + "러" + plate[3:]
            elif len(plate) == 6:
                # 2자리 + 한글(가) + 4자리 형태
                plate = plate[:2] + "가" + plate[2:]

        # 3. 위치 기반 한글 오타 수정 (뒤에서 5번째 자리)
        if len(plate) >= 7:
            target_idx = len(plate) - 5
            char = plate[target_idx]
            
            # 숫자나 영어로 오인된 한글 매핑 테이블
            kor_fix_map = {
                "2": "오", "5": "오", "0": "오",
                "4": "러", "7": "러", "L": "러",
                "8": "버", "6": "소", "B": "러", "H": "허", "A": "가"
            }
            
            if char in kor_fix_map:
                plate = plate[:target_idx] + kor_fix_map[char] + plate[target_idx+1:]
        
        return plate

    def select_best_candidate(self, candidate_list: list) -> str:
        """가장 점수가 높은 후보 선택"""
        best_plate = ""
        max_score = -1

        for c in candidate_list:
            # API 응답 구조에 맞게 'value'와 'score' 추출
            raw = c.get("value", "")
            confidence = c.get("score", 0)
            norm = self.normalize_plate(raw)

            # 점수 계산
            score = confidence
            if len(norm) == 8: score += 50  # 8자리 가중치
            if re.match(r"^\d{2,3}[가-힣]\d{4}$", norm): score += 30 # 한국 형식 가중치

            if score > max_score:
                max_score = score
                best_plate = norm
        return best_plate

    def extract_license_plate_from_url(self, image_url: str) -> str:
        print(f"\n🚀 [OCR 시작] URL: {image_url}")
        try:
            response = requests.get(image_url, timeout=10)
            image_bytes = response.content

            api_config = {"region": "kr", "mode": "accurate", "detection_mode": "shingle"}
            api_res = requests.post(
                self.api_url,
                data={"regions": ["kr"], "config": json.dumps(api_config)},
                files=dict(upload=image_bytes),
                headers={"Authorization": f"Token {self.api_token}"}
            )

            data = api_res.json()
            results = data.get("results", [])

            if not results:
                print("⚠️ 결과: 번호판 미검출")
                return "미검출"

            # [핵심 수정] Plate Recognizer의 실제 응답 구조인 props.plate에서 후보군 추출
            plate_info = results[0].get("plate", {})
            candidates = plate_info.get("props", {}).get("plate", [])

            if not candidates:
                # props 안에 없을 경우 상위 plate 값 사용
                top_plate = results[0].get("plate", "")
                return self.normalize_plate(top_plate)

            print("-" * 50)
            for i, cand in enumerate(candidates):
                raw = cand.get('value', 'N/A')
                conf = cand.get('score', 0)
                print(f"후보 {i+1} | 원본: {raw} | 신뢰도: {conf:.4f} | 보정: {self.normalize_plate(raw)}")
            print("-" * 50)

            final_result = self.select_best_candidate(candidates)
            print(f"✅ 최종 결과: {final_result}")
            return final_result

        except Exception as e:
            print(f"❌ 에러: {str(e)}")
            return "에러 발생"

ocr_service = OCRService()