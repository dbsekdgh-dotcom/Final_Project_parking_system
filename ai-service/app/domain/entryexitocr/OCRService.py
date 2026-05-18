import easyocr
import numpy as np
from pathlib import Path
import cv2
import re
import base64
import yaml
from openai import OpenAI
from ultralytics import YOLO

  # ── 경로 설정 ─────────────────────────────────────────────────────────
  # 이 파일(OCRService.py)부터 위로 올라가는 경로
  # parents[0] = entryexitocr 폴더
  # parents[1] = domain 폴더
  # parents[2] = app 폴더
  # parents[3] = ai-service 폴더
  # parents[4] = 프로젝트 루트 (Final_Project_parking_system)
_ROOT = Path(__file__).parents[4]
_AI_SERVICE_DIR = Path(__file__).parents[3]
_CONFIG_PATH = _ROOT / "ai" / "configs" / "entryexitocr" / "config.yml"
_PROMPT_PATH = _ROOT / "ai" / "prompts" / "entryexitocr" / "plate_correction.txt"

  # ── 모델 초기화 (서버 시작 시 딱 1번만 실행, 이후 재사용) ──────────────
model_path = _AI_SERVICE_DIR / "models" / "license_plate_detector.pt"
model = YOLO(str(model_path))          # 번호판 위치를 찾는 YOLO 모델
reader = easyocr.Reader(['ko', 'en'])  # 텍스트를 읽는 EasyOCR

  # ── 한국 번호판 정규식 ─────────────────────────────────────────────────
  # \d{2,3} = 숫자 2~3자리 / [가-힣] = 한글 1자 / \d{4} = 숫자 4자리
PLATE_PATTERN = r"\d{2,3}[가-힣]\d{4}"


  # ── config.yml 로더 ───────────────────────────────────────────────────
  # 매번 파일을 읽는 이유: 서버 재시작 없이 config.yml만 수정해도 즉시 반영되도록
def _load_config() -> dict:
    try:
        with open(_CONFIG_PATH, encoding="utf-8") as f:
            return yaml.safe_load(f)
    except FileNotFoundError:
        return {"model": "gpt-4o-mini", "max_tokens": 30, "confidence_threshold": 0.5}


def _load_prompt(ocr_candidates: list) -> str:
    try:
        with open(_PROMPT_PATH, encoding="utf-8") as f:
            template = f.read()
    except FileNotFoundError:
        template = (
            "OCR 후보: {ocr_candidates}\n"
            "한국 자동차 번호판을 읽어주세요.\n"
            "형식: 숫자2~3자리+한글1자+숫자4자리 (예: 12가1234)\n"
            "번호만 출력, 인식 불가시 인식실패만 출력."
        )
    candidate_text = (
        ", ".join([f'{p}(신뢰도:{s:.2f})' for p, s in ocr_candidates])
        if ocr_candidates else "없음"
    )
    return template.format(ocr_candidates=candidate_text)


  # ── 이미지 전처리 함수들 ───────────────────────────────────────────────
def enhance(image):
      # BGR 컬러 → 흑백 변환 (EasyOCR은 흑백에서 더 잘 읽음)
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
      # 명암 대비 강화 (어두운 번호판도 잘 보이게)
    gray = cv2.equalizeHist(gray)
      # 노이즈 제거 (카메라 잡음 줄이기)
    gray = cv2.GaussianBlur(gray, (3, 3), 0)
      # 샤프닝 (글자 경계선 선명하게)
    kernel = np.array([[0, -1, 0], [-1, 5, -1], [0, -1, 0]])
    return cv2.filter2D(gray, -1, kernel)


def deskew(image):
      # 번호판이 기울어진 경우 수평으로 보정
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    edges = cv2.Canny(gray, 50, 150)
    lines = cv2.HoughLines(edges, 1, np.pi / 180, 100)
    if lines is None:
        return image
    angle = np.mean([theta for _, theta in lines[:, 0]])
    angle = (angle - np.pi / 2) * 180 / np.pi
    h, w = image.shape[:2]
    M = cv2.getRotationMatrix2D((w // 2, h // 2), angle, 1)
    return cv2.warpAffine(image, M, (w, h))


def detect_plate(image):
      # YOLO로 이미지에서 번호판 위치(좌표)를 찾아 해당 영역만 잘라서 반환
    results = model(image)
    for r in results:
        for box in r.boxes.xyxy:
            x1, y1, x2, y2 = map(int, box)
            return image[y1:y2, x1:x2]  # 번호판 영역만 크롭
    return image  # 번호판 못 찾으면 원본 전체 반환


def extract_plate_pattern(text: str):
      # OCR이 읽은 텍스트에서 번호판 패턴 추출
    text = text.replace(" ", "")  # 공백 제거 (OCR이 "12가 1234"처럼 읽는 경우 대비)
    match = re.search(PLATE_PATTERN, text)
    return match.group() if match else None


  # ── numpy 이미지 배열 → base64 문자열 변환 ─────────────────────────────
  # Anthropic API는 이미지를 base64 문자열로 받음
  # cv2 이미지는 numpy 배열이므로 JPEG 바이트로 압축 후 base64 인코딩 필요
def _image_to_base64(image_np: np.ndarray) -> str:
    _, buffer = cv2.imencode('.jpg', image_np)  # numpy → JPEG 바이트
    return base64.b64encode(buffer).decode('utf-8')  # 바이트 → base64 문자열


  
async def _correct_with_llm(plate_image_np: np.ndarray, ocr_candidates: list) -> str:
    try:
        cfg = _load_config()

        client = OpenAI()

        response = client.chat.completions.create(
              model=cfg["model"],           # config.yml에서 gpt-4o-mini 로드
              max_tokens=cfg["max_tokens"], # config.yml에서 30 로드
              messages=[{
                  "role": "user",
                  "content": [
                      {
                          "type": "image_url",
                          "image_url": {
                              "url": f"data:image/jpeg;base64,{_image_to_base64(plate_image_np)}"
                          }
                      },
                      {
                          "type": "text",
                          "text": _load_prompt(ocr_candidates)
                      }
                  ]
              }]
        )

          
        result = response.choices[0].message.content.strip().replace(" ","")
        match = re.search(PLATE_PATTERN, result)
        if match:
            return match.group()

    except Exception as e:
        print(f"[LLM fallback 실패] {type(e).__name__}: {e}")
        

    return "인식 실패"


  # ── 메인 번호판 인식 함수 (router.py에서 호출) ─────────────────────────
async def extract_plate_number(file):
    cfg = _load_config()
    threshold = cfg["confidence_threshold"]  # 신뢰도 임계값 (config.yml에서)
    contents = await file.read()
    nparr = np.frombuffer(contents, np.uint8)
    image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
      # 1단계: YOLO로 번호판 영역 크롭
    plate_region = detect_plate(image)
      # 2단계: 전처리 (기울기 보정 → 흑백+선명화)
    plate_enhanced = enhance(deskew(plate_region))
      # 3단계: EasyOCR로 텍스트 인식
    candidates = []
    for _, text, score in reader.readtext(plate_enhanced):
        if score < 0.5:   # 50% 미만 신뢰도는 노이즈로 간주하고 버림
            continue
        plate = extract_plate_pattern(text)
        if plate:
            candidates.append((plate, score))

    if candidates:
        candidates.sort(key=lambda x: x[1], reverse=True)
        best_plate, best_score = candidates[0]
          # confidence_threshold 이상이면 신뢰할 수 있으므로 LLM 없이 반환
        if best_score >= threshold:
            return best_plate

      # 4단계: EasyOCR 실패 or 저신뢰도 → Claude Vision 재시도
    return await _correct_with_llm(plate_region, candidates)
