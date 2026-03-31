import json
import os
import re
import subprocess


class OCRService:
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
        plate = (
            first.get("plate")
            or first.get("plate_number")
            or first.get("plateNumber")
            or first.get("license_plate")
        )

        return str(plate).strip() if plate else ""


ocr_service = OCRService()