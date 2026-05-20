import os
import requests
from dotenv import load_dotenv
from langchain_core.tools import tool
import redis
import json

load_dotenv()
BACKEND_API_URL = os.getenv("BACKEND_API_URL", "http://localhost:8081")

REDIS_HOST = os.getenv("REDIS_HOST", "localhost")                                                                                                                                                                                                                                                  
REDIS_PORT = int(os.getenv("REDIS_PORT", "6379"))   
FEE_CACHE_KEY="fee_policy:cache"
FEE_CACHE_TTL=300

redis_client=redis.Redis(host=REDIS_HOST,port=REDIS_PORT,db=0,decode_responses=True)

@tool
def get_fee_policy(user_question)-> str:
    """
    사용자 질문에 맞는 주차장 요금 정책을 조회한다.
    외부인 요금, 방문객 요금, 할인권, 회차 인정 시간, 결제 후 출차 가능 시간,
    입주민 무료 정책, 정기권 무료 정책 질문에 사용한다.
    """
    try:
        cached=redis_client.get(FEE_CACHE_KEY)
        if cached:
            # redis에 있으면 불러오기
            data=json.loads(cached)
        else:
            # redis에 없는 경우
            response=requests.get(f"{BACKEND_API_URL}/api/kiosk/chatbot/fee_policy",timeout=5)
            #응답 실패인지 확인, 에러코드이면 에러 발생
            response.raise_for_status()
            #json응답을 python dict로 변환
            data=response.json()
            # redis 저장(setex : 만료시간 포함해서 저장, json문자열로 변환해서 저장)
            redis_client.setex(FEE_CACHE_KEY,FEE_CACHE_TTL, json.dumps(data,ensure_ascii=False))
        
        policies=data.get("policies",[])
        discount_tickets=data.get("discount_tickets",[])
        
        visit_policy=find_policy(policies,"VISIT")
        reservation_policy=find_policy(policies, "RESERVATION")
        
        query_type=detect_fee_type(user_question)
        
        if query_type=="visit":
            return format_policy(visit_policy)
        if query_type=="reservation":
            return (
                 f"{format_reservation_free_note()}\n\n"
                f"{format_policy(reservation_policy)}"
            )
        if query_type=="resident":
            return format_resident_note()
        if query_type=="season_ticket":
            return format_season_ticket_note()
        if query_type=="discount":
            return format_discount_tickets(discount_tickets)
        
        if query_type == "turnaround":
            return (
                "[회차 인정 시간]\n"
                f"- 외부인 회차 인정 시간: {visit_policy.get('turnaround_grace_minutes')}분\n"
                f"- 방문객 회차 인정 시간: {reservation_policy.get('turnaround_grace_minutes')}분"
            )
        if query_type == "post_payment_grace":
            return format_post_payment_grace(data.get("post_payment_grace_minutes"))

        if query_type == "reservation_free_note":
            return format_reservation_free_note()
        
        if query_type == "free_time_summary":
            return format_free_time_summary(
                visit_policy=visit_policy,
                reservation_policy=reservation_policy,
                post_payment_grace_minutes=data.get("post_payment_grace_minutes"),
            )
        if query_type == "all":
            return (
                "[요금/정책 DB 조회 결과]\n\n"
                f"{format_policy(visit_policy)}\n\n"
                f"{format_policy(reservation_policy)}\n\n"
                f"{format_post_payment_grace(data.get('post_payment_grace_minutes'))}\n\n"
                f"{format_discount_tickets(discount_tickets)}\n\n"
                f"{format_resident_note()}\n\n"
                f"{format_season_ticket_note()}\n\n"
                f"{format_reservation_free_note()}"
            )
        return (
            "요금 정책 질문이 명확하지 않습니다. "
            "외부인, 방문객, 할인권, 회차 인정 시간, 결제 후 출차 가능 시간 중 어떤 내용인지 확인이 필요합니다."
        )
        
    except Exception as e:
        print("요금 정책 조회 실패",repr(e))
        return "요금 정책 조회에 실패했습니다. 관리자에게 문의하세요"
    
    
def normalize_text(text)->str:
    if not text:
        return ""
    return text.replace(" ","").strip().lower()

def detect_fee_type(user_question)->str:
    """ 
     사용자가 어떤 요금 정책을 물어보는지 분류
    """
    question=normalize_text(user_question)
    
    if not question:
        return "unknown"
    
    #결제 후 출차 가능 시간
    if (
         "결제후" in question
        or "결제하고" in question
        or "정산후" in question
        or "출차가능" in question
        or "출차시간" in question
        or "결제후몇분" in question
    ):
        return "post_payment_grace"

    #회차 인정 시간
    if (
        "회차" in question
        or "무료회차" in question
        or "입차후" in question
        or "그냥나가" in question
        or "바로나가" in question
        or "잠깐들어" in question
    ):
        return "turnaround"
    
    #방문객 무료시간
    if (
        "방문객" in question
        or "방문자" in question
        or "방문신청" in question
    ) and ("무료" in question or "무료시간" in question):
        return "reservation_free_note"
    
    # 할인권
    if "할인권" in question or "할인" in question:
        return "discount"

    # 외부인 요금
    if "외부인" in question or "일반" in question or "비회원" in question:
        return "visit"

    # 방문객 요금
    if "방문객" in question or "방문자" in question:
        return "reservation"

    # 입주민 요금
    if "입주민" in question or "거주민" in question:
        return "resident"
    
    # 정기권 요금
    if "정기권" in question or "정기주차" in question:
        return "season_ticket"

    # 무료시간이라고만 물어본 경우
    if "무료" in question or "무료시간" in question:
        return "free_time_summary"

    # 전체 요금
    if "요금" in question or "금액" in question or "가격" in question or "정산" in question:
        return "all"

    return "unknown"

def find_policy(policies,parking_type):
    for policy in policies:
        if policy.get("parking_type")==parking_type:
            return policy
    return {}

def format_policy(policy):
    if not policy:
        return "해당 요금 정책을 찾지 못했습니다."
    
    label=policy.get("label")
    return(
        f"[{label} 요금 정책]\n"
        f"- 기본 요금: {policy.get('base_fee')}원\n"
        f"- 추가 요금: {policy.get('unit_minutes')}분마다 {policy.get('unit_fee')}원\n"
        f"- 일 최대 요금: {policy.get('daily_max_fee')}원\n"
        f"- 회차 인정 시간: {policy.get('turnaround_grace_minutes')}분"
    )
    
def format_discount_value(ticket):
    discount_type = ticket.get("discount_type")
    amount = ticket.get("discount_amount")

    if discount_type == "TIME":
        return f"{amount}분 할인"

    if discount_type == "AMOUNT":
        return f"{amount}원 할인"

    if discount_type == "PERCENT":
        return f"{amount}% 할인"

    if discount_type == "FREE":
        return "무료"

    # 타입이 없으면 일단 일반 표현
    if amount is not None:
        return f"{amount} 할인"

    return "할인 정보 없음"
    
def format_discount_tickets(discount_tickets):
    if not discount_tickets:
        return "[할인권 정책]\n- 등록된 할인권 정책이 없습니다."
    lines= ["[할인권 정책]"]
    
    for ticket in discount_tickets:
        lines.append(
            f"- {ticket.get('name')}: "
            f"{format_discount_value(ticket)}, "
            f"판매가 {ticket.get('price')}원"
        )
    return "\n".join(lines)

def format_post_payment_grace(post_payment_grace_minutes):
    return (
        "[결제 후 출차 가능 시간]\n"
        f"- 결제 완료 후 {post_payment_grace_minutes}분 이내에 출차해야 합니다."
    )
    
def format_resident_note():
    return (
        "[입주민 무료 정책]\n"
        "- 입주민은 무료입니다."
    )

def format_season_ticket_note():
    return (
        "[정기권 무료 정책]\n"
        "- 정기권 구매자는 정기권 구매 기간 동안 무료입니다."
    )
    
def format_reservation_free_note():
    return (
        "[방문객 무료시간 안내]\n"
        "- 방문객 무료시간은 입주민 방문객 신청 내역에 따라 달라질 수 있습니다.\n"
        "- 정확한 무료시간 적용 여부와 실제 요금은 결제 확인 화면에 표시된 금액을 확인해야 합니다."
    )
    
def format_free_time_summary(visit_policy, reservation_policy, post_payment_grace_minutes):
    return (
        "[무료시간 안내]\n"
        "- 무료시간은 상황에 따라 의미가 다를 수 있습니다.\n"
        f"- 회차 인정 시간: 외부인 {visit_policy.get('turnaround_grace_minutes')}분, "
        f"방문객 {reservation_policy.get('turnaround_grace_minutes')}분\n"
        f"- 결제 후 출차 가능 시간: {post_payment_grace_minutes}분\n"
        "- 입주민은 무료입니다.\n"
        "- 정기권 구매자는 정기권 구매 기간 동안 무료입니다.\n"
        "- 방문객 무료시간은 입주민 방문객 신청 내역에 따라 달라질 수 있으므로, "
        "실제 적용 여부는 결제 확인 화면에서 확인해야 합니다."
    )
    
# if __name__ == "__main__":
#     tests = [
#         "외부인 요금 알려줘",
#         "방문객 요금 알려줘",
#         "할인권 종류 알려줘",
#         "회차 인정 시간 알려줘",
#         "결제 후 몇 분 안에 나가야 해?",
#         "방문객 무료시간 알려줘",
#         "입주민은 요금 내?",
#         "정기권 구매자는 무료야?",
#         "무료시간 알려줘",
#         "주차 요금 정책 알려줘",
#     ]

#     for question in tests:
#         print("질문:", question)
#         print(get_fee_policy.invoke({"user_question": question}))
#         print("-" * 60)