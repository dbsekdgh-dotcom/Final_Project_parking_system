import os
from langchain_openai import ChatOpenAI
from langchain_core.messages import SystemMessage, HumanMessage

def generate_report_comment(data: dict, period: str) -> dict:
    """
    통계 데이터를 받아 LLM으로 분석 코멘트를 생성합니다.
    반환값: {
        "summary_comment": "전체 요약 분석",
        "revenue_comment": "매출 분석",
        "usage_comment": "사용량 분석"
    }
    """
    llm = ChatOpenAI(
        model="gpt-4o-mini",
        temperature=0.3,
        api_key=os.getenv("OPENAI_API_KEY")
    )
    
    period_label = "월간" if period == "MONTHLY" else "주간"
    
    prompt = f"""
다음은 주차장 관리 시스템의 {period_label} 통계 데이터입니다.
각 항목에 대해 운영 보고서에 들어갈 전문적인 한국어 분석 코멘트를 작성해주세요.

[전체 현황]
- 입주 세대수: {data.get('household_count')}세대 / {data.get('total_household_count')}세대
- 등록 차량: {data.get('vehicle_count')}대
- 주차공간 점유: {data.get('occupied_space')} / {data.get('total_space')}칸
- 주차요금 수익: {data.get('total_revanue')}원

[매출 현황]
- 순 매출액: {data.get('revenue_total')}원
- 전월 대비: {data.get('revenue_change_percent')}%
- 월별 추이: {data.get('revenue_monthly')}

[사용량 현황]
- 총 사용량: {data.get('usage_total_count')}건
- 전월 대비: {data.get('usage_change_percent')}%

아래 3가지 항목으로 나눠서 각각 2~3문장으로 작성해주세요.
각 항목은 반드시 아래 형식으로 구분해주세요:

[전체요약]
(내용)

[매출분석]
(내용)

[사용량분석]
(내용)
"""

    message = [
        SystemMessage(content="당신은 주차장 운영 전문 분석가입니다. 데이터를 바탕으로 간결하고 전문적인 보고서 코멘트를 작성합니다."),
        HumanMessage(content=prompt)
    ]
    
    response = llm.invoke(message)
    return _parse_comment(response.content)

def _parse_comment(text: str) -> dict:
    """LLM 응답을 섹션별로 파싱합니다."""
    result = {
        "summary_comment": "",
        "revenue_comment": "",
        "usage_comment": ""
    }
    sections = {
        "[전체요약]":"summary_comment",
        "[매출분석]":"revenue_comment",
        "[사용량분석]":"usage_comment"
    }
    current_key=None
    lines=text.strip().split("\n") # 줄 단위로 쪼개기
    for line in lines:
        stripped = line.strip()
        if stripped in sections:
            current_key = sections[stripped] # 섹션 제목 만나면 키 전환
        elif current_key and stripped:
            result[current_key] += stripped + " " # 내용은 현재 키에 누적
    
    return {k: v.strip() for k, v in result.items()}
    