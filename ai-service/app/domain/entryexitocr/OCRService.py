import easyocr
import numpy as np
import os
from pathlib import Path
import cv2
import re
from ultralytics import YOLO
BASE_DIR = Path(__file__).resolve().parents[3]
model_path = BASE_DIR / "models"/ "license_plate_detector.pt"
model=YOLO(str(model_path))
reader = easyocr.Reader(['ko','en'])
import cv2
import numpy as np
def enhance(image):
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    # 대비 증가
    gray = cv2.equalizeHist(gray)
    # 노이즈 제거
    gray = cv2.GaussianBlur(gray, (3,3), 0)
    # 샤프닝
    kernel = np.array([[0,-1,0],[-1,5,-1],[0,-1,0]])
    sharp = cv2.filter2D(gray, -1, kernel)

    return sharp
def deskew(image):
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    edges = cv2.Canny(gray, 50, 150)

    lines = cv2.HoughLines(edges, 1, np.pi/180, 100)

    if lines is None:
        return image

    angle = np.mean([theta for _,theta in lines[:,0]])
    angle = (angle - np.pi/2) * 180/np.pi

    h, w = image.shape[:2]
    M = cv2.getRotationMatrix2D((w//2, h//2), angle, 1)
    return cv2.warpAffine(image, M, (w, h))

def detect_plate(image):
    results = model(image)
    
    for r in results:
        for box in r.boxes.xyxy:
            x1,y1,x2,y2=map(int,box)
            plate_img=image[y1:y2,x1:x2]
            return plate_img
    return image

def extract_plate_pattern(text: str):
    text = text.replace(" ","")
    PLATE_PATTERN = r"\d{2,3}[가-힣]\d{4}"
    match = re.search(PLATE_PATTERN, text)
    
    if match:
        return match.group()
    return None

async def extract_plate_number(file):
    
    contents = await file.read()
    
    nparr = np.frombuffer(contents, np.uint8)
    image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
    
    plate_regione=detect_plate(image)
    plate_regione=deskew(plate_regione)
    plate_regione=enhance(plate_regione)
    result = reader.readtext(plate_regione)
    candidates=[]
    
    for bbox,text,score in result:
        
        if score < 0.5:
            continue
        plate= extract_plate_pattern(text)
        if plate:
            candidates.append((plate,score))
    if not candidates:
        return "인식 실패"
    
    candidates.sort(key=lambda x: x[1], reverse=True)
    return candidates[0][0]
    