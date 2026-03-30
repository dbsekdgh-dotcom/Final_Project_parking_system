# 최신 안정화 버전인 3.11 또는 3.12 슬림 이미지를 사용합니다.
FROM python:3.12-slim

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. 라이브러리 목록 복사
COPY requirements.txt .

# 4. 라이브러리 설치
RUN pip install --no-cache-dir -r requirements.txt

# 5. 소스 코드 복사
COPY . .

# 6. 실행 명령 (파일명이 main.py가 아니라면 수정하세요)
CMD ["python", "main.py"]