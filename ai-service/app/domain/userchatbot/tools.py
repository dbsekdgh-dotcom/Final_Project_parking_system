import httpx
import os
from typing import Annotated
from dotenv import load_dotenv
from langchain_core.tools import tool
from langgraph.prebuilt import InjectedState

load_dotenv()
SPRING_URL = os.getenv("SPRING_API_URL")

def get_headers(token: str):
    return {"Authorization": f"Bearer {token}"}

def parse_response(response):
    if response.status_code == 401:
        return {"error": True, "message": "로그인 세션이 만료되었습니다. 다시 로그인해 주세요.", "status": 401}
    if response.status_code == 204 or not response.content:
        return {"success": True}
    if response.status_code >= 400:
        try:
            body = response.json()
            msg = body.get("message") or body.get("error") or str(body)
        except Exception:
            msg = response.text
        return {"error": True, "message": msg, "status": response.status_code}
    return response.json()

# ==========================================
# 1. 방문 예약 관련 도구
# ==========================================

@tool
async def get_my_reservations(access_token: Annotated[str, InjectedState("access_token")]):
    """사용자의 전체 방문 예약 목록을 조회합니다. (조회/내역 확인용)"""
    async with httpx.AsyncClient() as client:
        response = await client.get(
            f"{SPRING_URL}/api/user/reservations",
            headers=get_headers(access_token)
        )
        return parse_response(response)

@tool
async def get_cancellable_reservations(access_token: Annotated[str, InjectedState("access_token")]):
    """취소 가능한 방문 예약 목록만 조회합니다. PENDING/RESERVED 상태이면서 내일 이후 예약만 반환됩니다. (취소용)"""
    from datetime import date, datetime
    async with httpx.AsyncClient() as client:
        response = await client.get(
            f"{SPRING_URL}/api/user/reservations",
            headers=get_headers(access_token)
        )
    result = parse_response(response)
    if isinstance(result, list):
        today = date.today()
        def is_future(r):
            try:
                visit = r.get("visitStartAt", "")
                return datetime.fromisoformat(visit[:10]).date() > today
            except Exception:
                return False
        filtered = [r for r in result if r.get("status") in ("PENDING", "RESERVED") and is_future(r)]
        # 번호와 reservationId를 명시적으로 분리해서 LLM이 번호를 ID로 착각하지 않도록 함
        return [{"번호": i + 1, "reservationId": r["reservationId"], "carNumber": r.get("carNumber"), "visitStartAt": r.get("visitStartAt"), "purpose": r.get("purpose"), "status": r.get("status")} for i, r in enumerate(filtered)]
    return result

@tool
async def create_reservation(
    car_number: str,
    visit_start_at: str,
    purpose: str,
    access_token: Annotated[str, InjectedState("access_token")]
):
    """
    방문 예약을 신청합니다.
    - car_number: 차량번호 (예: 12가3456)
    - visit_start_at: 방문 시작 시간 (ISO 형식, 예: 2026-05-10T14:00:00)
    - purpose: 방문 목적 (FAMILY, FRIEND, BUSINESS, DELIVERY, OTHER 중 하나)
    """
    payload = {
        "carNumber": car_number,
        "visitStartAt": visit_start_at,
        "purpose": purpose
    }
    async with httpx.AsyncClient() as client:
        response = await client.post(
            f"{SPRING_URL}/api/user/reservations",
            headers=get_headers(access_token),
            json=payload
        )
        return parse_response(response)

@tool
async def cancel_reservation(
    reservation_id: int,
    access_token: Annotated[str, InjectedState("access_token")]
):
    """예약 ID로 방문 예약을 취소합니다."""
    async with httpx.AsyncClient() as client:
        response = await client.patch(
            f"{SPRING_URL}/api/user/reservations/{reservation_id}/cancel",
            headers=get_headers(access_token)
        )
        return parse_response(response)

@tool
async def cancel_reservation_by_date(
    car_number: str,
    visit_start_at: str,
    access_token: Annotated[str, InjectedState("access_token")]
):
    """차량번호와 방문 시작 시간으로 예약을 취소합니다. visit_start_at은 ISO 형식(예: 2026-05-02T14:00:00)으로 전달하세요."""
    async with httpx.AsyncClient() as client:
        response = await client.get(
            f"{SPRING_URL}/api/user/reservations",
            headers=get_headers(access_token)
        )
    reservations = parse_response(response)
    if not isinstance(reservations, list):
        return reservations

    target = next(
        (r for r in reservations
         if r.get("carNumber") == car_number
         and r.get("visitStartAt", "").startswith(visit_start_at[:16])),
        None
    )
    if not target:
        return {"error": True, "message": "해당 예약을 찾을 수 없습니다."}

    reservation_id = target["reservationId"]
    async with httpx.AsyncClient() as client:
        response = await client.patch(
            f"{SPRING_URL}/api/user/reservations/{reservation_id}/cancel",
            headers=get_headers(access_token)
        )
    return parse_response(response)

# ==========================================
# 2. 사용자 정보
# ==========================================

@tool
async def get_my_info(access_token: Annotated[str, InjectedState("access_token")]):
    """로그인한 사용자의 회원 정보를 조회합니다. userStatus: 일반 회원 / 입주민 신청 중 / 입주민"""
    async with httpx.AsyncClient() as client:
        response = await client.get(
            f"{SPRING_URL}/api/user/mypage",
            headers=get_headers(access_token)
        )
        return parse_response(response)

# ==========================================
# 3. 주차 현황 및 포인트
# ==========================================

@tool
async def get_parking_summary(access_token: Annotated[str, InjectedState("access_token")]):
    """아파트 단지 내 층별 잔여 주차 자리를 조회합니다. (B1: 방문자층, B2: 입주민층)"""
    async with httpx.AsyncClient() as client:
        r_resident = await client.get(
            f"{SPRING_URL}/api/user/space/summary",
            params={"userType": "RESIDENT"},
            headers=get_headers(access_token)
        )
        r_visitor = await client.get(
            f"{SPRING_URL}/api/user/space/summary",
            params={"userType": "VISITOR"},
            headers=get_headers(access_token)
        )
    resident = parse_response(r_resident)
    visitor = parse_response(r_visitor)
    if isinstance(resident, dict) and resident.get("error"):
        return resident
    if isinstance(visitor, dict) and visitor.get("error"):
        return visitor
    return {"B2_입주민": resident, "B1_방문자": visitor}

@tool
async def get_my_points(access_token: Annotated[str, InjectedState("access_token")]):
    """사용자가 현재 보유 중인 포인트를 조회합니다."""
    async with httpx.AsyncClient() as client:
        response = await client.get(
            f"{SPRING_URL}/api/user/subscriptions/point",
            headers=get_headers(access_token)
        )
        return parse_response(response)

# ==========================================
# 3. 정기권
# ==========================================

@tool
async def initiate_subscription_purchase(
    start_date: str,
    access_token: Annotated[str, InjectedState("access_token")]
):
    """
    정기권 구매 페이지로 이동을 준비합니다. 시작일이 확정되면 이 도구를 호출하세요.
    - start_date: 정기권 시작일 (ISO 형식, 예: 2026-05-10)
    """
    from datetime import date
    try:
        if date.fromisoformat(start_date) < date.today():
            return {"error": True, "message": f"오늘({date.today().strftime('%m월 %d일')}) 이전 날짜로는 정기권을 구매할 수 없습니다. 오늘 또는 이후 날짜로 다시 말씀해 주세요."}
    except ValueError:
        return {"error": True, "message": "날짜 형식이 올바르지 않습니다. (예: 2026-05-10)"}
    async with httpx.AsyncClient() as client:
        resp = await client.get(
            f"{SPRING_URL}/api/user/vehicles/me",
            headers=get_headers(access_token)
        )
    if resp.status_code == 204 or not resp.content:
        return {"error": True, "message": "정기권 구매를 위해서는 활성화된 차량이 등록되어 있어야 합니다."}
    vehicle = parse_response(resp)
    if isinstance(vehicle, dict) and vehicle.get("error"):
        return vehicle
    if vehicle.get("status") != "ACTIVE":
        return {"error": True, "message": "정기권 구매를 위해서는 활성화된 차량이 등록되어 있어야 합니다."}
    return {"ready": True, "startDate": start_date}

@tool
async def get_my_subscriptions(access_token: Annotated[str, InjectedState("access_token")]):
    """사용자가 이용 중인 정기권 목록과 만료일을 조회합니다."""
    async with httpx.AsyncClient() as client:
        response = await client.get(
            f"{SPRING_URL}/api/user/subscriptions/my",
            headers=get_headers(access_token)
        )
        return parse_response(response)

@tool
async def get_subscription_policy(access_token: Annotated[str, InjectedState("access_token")]):
    """정기권 가격, 기간 등 구매 정책을 조회합니다. 구매 안내 후 페이지 이동을 유도합니다."""
    async with httpx.AsyncClient() as client:
        response = await client.get(
            f"{SPRING_URL}/api/user/subscriptions/policy",
            headers=get_headers(access_token)
        )
        return parse_response(response)

# ==========================================
# 4. 차량 등록
# ==========================================

@tool
async def initiate_vehicle_register(
    access_token: Annotated[str, InjectedState("access_token")]
):
    """차량 등록 페이지로 이동을 준비합니다."""
    async with httpx.AsyncClient() as client:
        resp = await client.get(
            f"{SPRING_URL}/api/user/vehicles/me",
            headers=get_headers(access_token)
        )
    if resp.status_code != 204 and resp.content:
        vehicle = parse_response(resp)
        if not (isinstance(vehicle, dict) and vehicle.get("error")):
            status = vehicle.get("status")
            if status == "ACTIVE":
                return {"error": True, "message": "이미 등록된 차량이 있습니다. 차량은 1대만 등록 가능합니다."}
            if status == "PENDING":
                return {"error": True, "message": "차량 등록 승인 대기 중입니다."}
    return {"ready": True}

@tool
async def cancel_vehicle_register(
    access_token: Annotated[str, InjectedState("access_token")]
):
    """승인 대기 중인 차량 등록 신청을 취소합니다. vehicleId와 approvalId는 내부에서 자동 조회합니다."""
    async with httpx.AsyncClient() as client:
        resp = await client.get(
            f"{SPRING_URL}/api/user/vehicles/me",
            headers=get_headers(access_token)
        )
    if resp.status_code == 204 or not resp.content:
        return {"error": True, "message": "등록된 차량이 없습니다."}
    vehicle = parse_response(resp)
    if isinstance(vehicle, dict) and vehicle.get("error"):
        return vehicle
    if vehicle.get("status") != "PENDING":
        return {"error": True, "message": "취소할 수 있는 차량 등록 신청이 없습니다. (승인 대기 중인 신청만 취소 가능합니다.)"}
    async with httpx.AsyncClient() as client:
        response = await client.post(
            f"{SPRING_URL}/api/user/vehicles/cancel",
            headers=get_headers(access_token),
            json={"vehicleId": vehicle["vehicleId"], "approvalId": vehicle["approvalId"]}
        )
        return parse_response(response)

# ==========================================
# 5. 입주민 신청
# ==========================================

@tool
async def get_available_units(access_token: Annotated[str, InjectedState("access_token")]):
    """현재 입주 신청이 가능한 호수 목록을 조회합니다. householdId와 unitNo를 함께 반환합니다."""
    async with httpx.AsyncClient() as client:
        response = await client.get(
            f"{SPRING_URL}/api/user/apply/unit-status",
            headers=get_headers(access_token)
        )
    result = parse_response(response)
    if isinstance(result, list):
        return [{"householdId": u["householdId"], "unitNo": u["unitNo"]}
                for u in result if u.get("status") == "AVAILABLE"]
    return result

@tool
async def apply_resident(
    unit_no: int,
    access_token: Annotated[str, InjectedState("access_token")]
):
    """호수 번호(예: 101)로 입주민 신청을 진행합니다. householdId는 내부에서 자동으로 조회합니다."""
    async with httpx.AsyncClient() as client:
        status_response = await client.get(
            f"{SPRING_URL}/api/user/apply/unit-status",
            headers=get_headers(access_token)
        )
    units = parse_response(status_response)
    if not isinstance(units, list):
        return units
    target = next((u for u in units if u.get("unitNo") == unit_no), None)
    if not target:
        return {"error": True, "message": f"{unit_no}호는 신청 가능한 호수가 아닙니다."}
    household_id = target["householdId"]
    async with httpx.AsyncClient() as client:
        response = await client.post(
            f"{SPRING_URL}/api/user/apply/resident",
            headers=get_headers(access_token),
            json={"householdId": household_id}
        )
        return parse_response(response)

@tool
async def cancel_resident_apply(
    access_token: Annotated[str, InjectedState("access_token")]
):
    """진행 중인 입주민 신청을 취소합니다. approvalId는 내부에서 자동으로 조회합니다."""
    async with httpx.AsyncClient() as client:
        status_resp = await client.get(
            f"{SPRING_URL}/api/user/apply/status",
            headers=get_headers(access_token)
        )
    status = parse_response(status_resp)
    if isinstance(status, dict) and status.get("error"):
        return status
    if status.get("userStatus") != "PENDING":
        return {"error": True, "message": "취소할 수 있는 입주민 신청이 없습니다."}
    approval_id = status.get("activeApprovalId")
    if not approval_id:
        return {"error": True, "message": "신청 정보를 찾을 수 없습니다."}
    async with httpx.AsyncClient() as client:
        response = await client.patch(
            f"{SPRING_URL}/api/user/apply/resident/{approval_id}/cancel",
            headers=get_headers(access_token)
        )
        return parse_response(response)
