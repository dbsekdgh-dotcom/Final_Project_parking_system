from fastapi import APIRouter, Cookie, HTTPException
from typing import Optional
from langchain_core.messages import HumanMessage, AIMessage
from .graph import app_graph
import logging
import re
import httpx
import os
import base64
import json
import time

SPRING_URL = os.getenv("SPRING_API_URL", "http://localhost:8080")

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/chatbot", tags=["AI 주차 비서"])

ENUM_KO = {
    # 방문 예약 목적
    "FAMILY": "가족", "FRIEND": "친구", "BUSINESS": "업무",
    "DELIVERY": "배달", "OTHER": "기타",

    # 방문 예약 상태
    "PENDING": "승인대기", "RESERVED": "예약완료", "CANCELLED": "취소됨",
    "NO_SHOW": "무단 미방문", "ENTERED": "주차중", "COMPLETED": "이용완료",
    "REJECTED": "거절됨",

    # 정기권 상태
    "ACTIVE": "이용중", "EXPIRED": "만료", "REFUNDED": "환불됨",

    # 주차 공간 상태
    "AVAILABLE": "주차가능", "OCCUPIED": "사용중", "BLOCKED": "폐쇄",

    # 입출차 상태
    "DETECTED": "감지됨", "ENTRY_CANCELLED": "입차취소", "EXIT_REQUESTED": "출차요청",
    "EXITED": "출차완료", "FORCE_EXITED": "강제출차", "BLACKLIST_REJECTED": "차단거부",

    # 결제 상태
    "UNPAID": "미결제", "PAID": "결제완료",

    # 주차 유형
    "RESIDENT": "입주민", "SUBSCRIPTION": "정기권", "RESERVATION": "예약방문",
    "VISIT": "방문",

    # 승인 상태
    "APPROVED": "승인됨",

    # 블랙리스트 상태
    "RELEASED": "해제됨",

    # 블랙리스트 사유
    "REPORT_ACCUMULATION": "신고 누적", "ILLEGAL_VEHICLE": "불법 차량",
    "USER_BLACKLIST": "사용자 신고", "ADMIN_MANUAL": "관리자 등록",
    "SYSTEM_BLOCK": "시스템 차단",

    # 층 구분
    "B1": "지하 1층", "B2": "지하 2층",
}

def _convert_iso_date(m: re.Match) -> str:
    from datetime import datetime
    raw = m.group(0).replace(" ", "T").rstrip("Z")
    try:
        dt = datetime.fromisoformat(raw[:16])
    except ValueError:
        return m.group(0)
    hour = dt.hour
    ampm = "오전" if hour < 12 else "오후"
    hour12 = hour if hour <= 12 else hour - 12
    minute = f" {dt.minute}분" if dt.minute else ""
    return f"{dt.month}월 {dt.day}일 {ampm} {hour12}시{minute}"

def _convert_time_24h(m: re.Match) -> str:
    hour, minute = int(m.group(1)), int(m.group(2))
    ampm = "오전" if hour < 12 else "오후"
    hour12 = hour if hour <= 12 else hour - 12
    return f"{ampm} {hour12}시" + (f" {minute}분" if minute else "")

def postprocess(text: str) -> str:
    # 마크다운 제거
    text = re.sub(r'\*\*(.+?)\*\*', r'\1', text)
    text = re.sub(r'\*(.+?)\*', r'\1', text)
    text = re.sub(r'^#{1,6}\s+', '', text, flags=re.MULTILINE)
    text = re.sub(r'^[-•]\s+', '', text, flags=re.MULTILINE)

    # 예약 ID 노출 제거 (마크다운 제거 후 처리)
    text = re.sub(r'[-\s]*예약 ?ID\s*:\s*\d+\s*\n?', '', text)

    # 도구 호출 전 예고 문구 제거
    waiting_patterns = [
        r'먼저[^。\n]*(?:조회|확인|진행)[^。\n]*(?:하겠습니다|할게요)[^\n]*\n?',
        r'잠시만[^。\n]*기다려[^。\n]*(?:주세요|주십시오)[^\n]*\n?',
        r'(?:예약|목록|정보)[^。\n]*(?:조회|확인)하겠습니다[^\n]*\n?',
    ]
    for pattern in waiting_patterns:
        text = re.sub(pattern, '', text)

    # ISO 날짜 변환 (2026-05-04T14:00:00 or 2026-05-04 14:00)
    text = re.sub(r'\d{4}-\d{2}-\d{2}[T ]\d{2}:\d{2}(?::\d{2})?', _convert_iso_date, text)

    # 24시간 시간 단독 변환 (14:00, 09:30 형태 - 날짜 변환 후 남은 것)
    text = re.sub(r'\b([01]?\d|2[0-3]):([0-5]\d)\b', _convert_time_24h, text)

    # 영어 enum 값 치환 (단독 or 괄호 안)
    for en, ko in ENUM_KO.items():
        text = re.sub(rf'\({en}\)', f'({ko})', text)
        text = re.sub(rf'\b{en}\b', ko, text)

    return text.strip()

def is_token_expired(token: str) -> bool:
    """JWT payload의 exp 클레임을 서명 검증 없이 확인합니다."""
    try:
        payload_b64 = token.split(".")[1]
        payload_b64 += "=" * (4 - len(payload_b64) % 4)
        payload = json.loads(base64.b64decode(payload_b64))
        exp = payload.get("exp", 0)
        return time.time() >= exp
    except Exception:
        return False


async def try_refresh_access_token(refresh_token: str) -> Optional[str]:
    """refreshToken 쿠키로 Spring에 토큰 갱신을 요청하고 새 accessToken 값을 반환합니다."""
    try:
        async with httpx.AsyncClient() as client:
            response = await client.post(
                f"{SPRING_URL}/api/user/auth/refresh",
                cookies={"refreshToken": refresh_token},
                timeout=5.0
            )
        if response.status_code != 200:
            logger.warning(f"토큰 갱신 실패: status={response.status_code}")
            return None
        # 응답 Set-Cookie 헤더에서 accessToken 값 추출
        # httpx는 동일 헤더가 여러 개일 때 multi_items()로 접근
        set_cookies = [v for k, v in response.headers.multi_items() if k.lower() == "set-cookie"]
        for header_value in set_cookies:
            if "accessToken=" in header_value:
                for part in header_value.split(";"):
                    part = part.strip()
                    if part.startswith("accessToken="):
                        return part.split("=", 1)[1]
        return None
    except Exception as e:
        logger.error(f"토큰 갱신 중 예외: {e}")
        return None


@router.post("/ask")
async def chat_with_bot(
    request: dict,
    accessToken: Optional[str] = Cookie(None),
    refreshToken: Optional[str] = Cookie(None),
):

    user_input = request.get("message")
    history = request.get("history", [])

    if not user_input:
        logger.warning("BadRequest: 'message' field is missing in request body.")
        raise HTTPException(status_code=400, detail="메시지가 없습니다.")

    token = accessToken or ""
    refresh = refreshToken or ""

    if not token and not refresh:
        logger.warning("Unauthorized: No accessToken or refreshToken cookie provided.")
    elif refresh and (not token or is_token_expired(token)):
        # accessToken이 없거나 만료된 경우 refreshToken으로 갱신
        logger.info("accessToken 만료 또는 없음, refreshToken으로 갱신 시도")
        refreshed = await try_refresh_access_token(refresh)
        if refreshed:
            token = refreshed
            logger.info("토큰 갱신 성공")
        else:
            logger.warning("토큰 갱신 실패 - 기존 토큰으로 진행")

    prior_messages = []
    for msg in history:
        if msg.get("role") == "user":
            prior_messages.append(HumanMessage(content=msg["text"]))
        elif msg.get("role") == "bot":
            prior_messages.append(AIMessage(content=msg["text"]))
    prior_messages.append(HumanMessage(content=user_input))

    try:
        inputs = {
            "messages": prior_messages,
            "access_token": token,
            "refresh_token": refresh,
            "context": {}
        }
        
        # 3. LangGraph 실행 (AI 엔진 및 도구 호출)
        # ainvoke 내부에서 발생하는 에러를 세밀하게 잡기 위해 try-except를 감쌉니다.
        final_state = await app_graph.ainvoke(inputs)
        
        if not final_state or "messages" not in final_state:
            raise ValueError("LangGraph returned an empty or invalid state.")

        final_answer = postprocess(final_state["messages"][-1].content)
        return {"reply": final_answer}

    except ValueError as ve:
        # 데이터 구조 문제 등 로직 에러
        logger.error(f"Logic Error: {str(ve)}")
        raise HTTPException(status_code=500, detail="응답 데이터 처리 중 에러가 발생했습니다.")

    except Exception as e:
        # [AWS 원인 파악용 핵심] 
        # 에러의 타입과 상세 메시지를 로그에 남겨야 CloudWatch에서 추적이 가능합니다.
        error_type = type(e).__name__
        logger.error(f"Unexpected Error [{error_type}]: {str(e)}", exc_info=True)
        
        # 보안을 위해 사용자에게는 상세 에러를 노출하지 않고 공통 메시지만 전달
        raise HTTPException(
            status_code=500, 
            detail=f"챗봇 서버 내부 에러가 발생했습니다. (Type: {error_type})"
        )