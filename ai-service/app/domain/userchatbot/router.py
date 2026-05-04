from fastapi import APIRouter, Cookie, HTTPException
from typing import Optional
from langchain_core.messages import HumanMessage, AIMessage
from .graph import app_graph
import logging
import re
import os
import base64
import json
import time
from langchain_core.messages import ToolMessage


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

    # 도구 호출 전 예고 문구 제거 - 마침표까지 포함해서 제거
    waiting_patterns = [
        r'^[^\n]*(?:잠시만|먼저)[^。\n]*(?:주세요|주십시오|하겠습니다|드리겠습니다|할게요)[.。!?]*\s*\n?',
        r'^[^\n]{0,30}(?:예약|신청|취소|조회|확인|진행|처리)(?:해\s*드리겠습니다|하겠습니다|시작하겠습니다|진행하겠습니다|처리하겠습니다)[.。!?]*\s*\n?',
        r'^[^\n]*(?:상태를\s*확인|확인해)[^。\n]*(?:드리겠습니다|하겠습니다)[.。!?]*\s*\n?',
    ]
    for pattern in waiting_patterns:
        text = re.sub(pattern, '', text, flags=re.MULTILINE)

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
    elif is_token_expired(token):
        # 토큰 만료 시 서버사이드 refresh를 하지 않고 브라우저 인터셉터에 위임
        # (서버사이드 refresh는 RTR 충돌 유발 - 브라우저와 동시에 rotate 시도)
        logger.info("accessToken 만료 - 브라우저 인터셉터에 갱신 위임")
        return {"reply": "로그인 세션이 만료되었습니다. 잠시 후 다시 시도해 주세요.", "action": None, "reservations": None, "subscriptionStartDate": None, "availableUnits": None}

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

        raw_content = final_state["messages"][-1].content
        final_answer = postprocess(raw_content)
        if len(final_answer.strip()) <= 2:
            # postprocess가 전부 제거하거나 마침표만 남긴 경우 마크다운만 걷어낸 원문 사용
            final_answer = re.sub(r'\*\*(.+?)\*\*', r'\1', raw_content).strip()
            final_answer = re.sub(r'\*(.+?)\*', r'\1', final_answer).strip()

        action = None
        reservations = None
        available_units = None
        subscription_start_date = None
        for msg in final_state["messages"]:
            # tool 호출 감지 (AIMessage.tool_calls)
            tool_calls = getattr(msg, "tool_calls", None)
            if tool_calls:
                for tc in tool_calls:
                    name = tc.get("name") if isinstance(tc, dict) else getattr(tc, "name", None)
                    args = tc.get("args") if isinstance(tc, dict) else getattr(tc, "args", {})
                    if name == "create_reservation":
                        action = "RESERVATION_CREATED"
                    elif name in ("cancel_reservation", "cancel_reservation_by_date"):
                        action = "RESERVATION_CANCELLED"
                    elif name == "initiate_vehicle_register":
                        action = "VEHICLE_REGISTER"
                    elif name == "cancel_vehicle_register":
                        action = "VEHICLE_REGISTER_CANCELLED"
                    elif name == "apply_resident":
                        action = "RESIDENT_APPLIED"
                    elif name == "cancel_resident_apply":
                        action = "RESIDENT_CANCELLED"
            # ToolMessage 결과 추출
            msg_name = getattr(msg, "name", None)
            if msg_name == "get_my_reservations":
                try:
                    content = msg.content
                    data = json.loads(content) if isinstance(content, str) else content
                    if isinstance(data, list):
                        reservations = data
                except Exception:
                    pass
            elif msg_name == "get_available_units":
                try:
                    content = msg.content
                    data = json.loads(content) if isinstance(content, str) else content
                    if isinstance(data, list):
                        available_units = [u.get("unitNo") for u in data if u.get("unitNo")]
                except Exception:
                    pass
            elif msg_name == "initiate_subscription_purchase":
                try:
                    content = msg.content
                    data = json.loads(content) if isinstance(content, str) else content
                    if isinstance(data, dict) and data.get("ready"):
                        action = "SUBSCRIPTION_PURCHASE"
                        subscription_start_date = data.get("startDate")
                except Exception:
                    pass

        # 액션 없이 도구만 호출하고 끝난 거절/안내 응답은 terminal=True → 프론트에서 히스토리 리셋
        # 단, get_available_units 호출 시엔 사용자가 호수를 선택해야 하므로 히스토리 유지
        has_available_units = any(
            getattr(m, "name", None) == "get_available_units" for m in final_state["messages"]
        )
        terminal = action is None and not has_available_units and any(
            isinstance(m, ToolMessage) for m in final_state["messages"]
        )

        return {"reply": final_answer, "action": action, "reservations": reservations,
                "subscriptionStartDate": subscription_start_date, "availableUnits": available_units,
                "terminal": terminal}

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