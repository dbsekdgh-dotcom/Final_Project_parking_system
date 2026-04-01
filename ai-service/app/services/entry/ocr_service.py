import json
import os
import re
import subprocess


class OCRService:
    def is_valid_korean_plate(self, plate: str) -> bool:
        pattern = r"^\d{2,3}[가-힣]\d{4}$"
        return bool(re.match(pattern, plate))
    
    def extract_license_plate(self, image_path: str) -> str:
        image_dir = os.path.abspath(os.path.dirname(image_path))
        image_name = os.path.basename(image_path)

        # openalpr 컨테이너에서 JSON(-j)으로 결과를 뽑아내고 plate 값을 추출합니다.
        command = [
            "docker",
            "run",
            "--rm",
            "-v",
            f"{image_dir}:/data",
            "openalpr/openalpr",
            "openalpr",
            "-c",
            "kr",
            "-j",
            f"/data/{image_name}",
        ]

        try:
            proc = subprocess.run(command, capture_output=True, text=True, check=False)
        except Exception as e:
            return f"Error: {str(e)}"

        stdout = proc.stdout or ""
        stderr = proc.stderr or ""

        if proc.returncode != 0:
            # 컨테이너 실행 문제(이미지 없음 등)일 수 있으므로 raw 로그를 뒤에 반환
            return f"Error: OCR failed (exit={proc.returncode}): {stderr.strip() or stdout.strip()}"

        # 기대 포맷: openalpr -j 출력(JSON)
        try:
            data = json.loads(stdout)
        except Exception:
            # JSON 파싱 실패 시 fallback: plate 문자열 추출
            match = re.search(r"plate[^A-Za-z0-9]*[:=]\s*([A-Za-z0-9가-힣\-]+)", stdout, re.IGNORECASE)
            return match.group(1).strip() if match else stdout.strip()

        results = data.get("results") or []
        if not results:
            return ""

        first = results[0] or {}
        candidates = first.get("candidates",[])
        if candidates:
            plate = self.select_best_candidate(candidates)
        else :
            plate = first.get("plate","")
            plate = self.normalize_plate(plate)
        
        return plate
    
    def is_valid_korean_plate(self, plate: str) -> bool:
        pattern = r"^\d{2,3}[가-힣]\d{4}$"
        return bool(re.match(pattern, plate))
    
    def normalize_plate(self, plate: str) -> str:
        if not plate:
            return ""

        # 1. 공백/특수문자 제거
        plate = re.sub(r"[^0-9A-Za-z가-힣]", "", plate)

        # 2. 길이 보정 (7~8자리만 허용)
        if len(plate) < 7 or len(plate) > 8:
            return plate

        # 한글 자리 교정
        kor_fix_map = {
            "B": "러",
            "H": "허",
            "A": "가",
            "O": "오",
            "U": "우",
            "8": "버",
        }

        # 한국 번호판: 숫자 + 한글 + 숫자 구조
        # ex) 123러4567 → index 3 또는 2가 한글
        idx = 3 if len(plate) == 8 else 2

        ch = plate[idx]

        if ch in kor_fix_map:
            plate = plate[:idx] + kor_fix_map[ch] + plate[idx + 1:]

        return plate

    def select_best_candidate(self,candidates: list) -> str:
        best_plate = ""
        best_score = -1
        
        for c in candidates:
            plate = c.get("plate","")
            confidence = c.get("confidence",0)
            
            normalized = self.normalize_plate(plate)
            
            score = confidence
            
            if self.is_valid_korean_plate(normalized):
                score += 30
            if re.search(r"[가-힣]",normalized):
                score += 10
            if re.search(r"[A-Za-z]",normalized):
                score -= 15
            
            if score> best_score:
                best_score=score
                best_plate=normalized
            return best_plate
ocr_service = OCRService()