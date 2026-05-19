from prompts.kiosk_chatbot_prompt import KIOSK_SYSTEM_PROMPT,KIOSK_USER_PROMPT_TEMPLATE,SCREEN_GUIDE, KIOSK_FEE_PROMPT_TEMPLATE
from prompts.kiosk_navigation import STORE_LOGGED_IN_SCREEN_IDS, get_navigation_response,contains_keyword
from app.domain.kioskChatbot.kiosk_tools import get_fee_policy, detect_fee_type
from app.domain.kioskChatbot.kiosk_vectorstore import get_retriever
from app.domain.kioskChatbot.kiosk_redis import get_messages,format_messages,save_message
import os
from langchain_openai import ChatOpenAI
from langchain_core.messages import SystemMessage, HumanMessage, AIMessage,ToolMessage


def get_screen_guide(screen_id=None):
    """ 
    screen_id에 맞는 화면 안내 문구를 가져온다.
    screen_id가 없으면 화면 정보 없음으로 처리한다.
    """
    
    if not screen_id:
        return ("현재 화면에 대한 정보가 제공되지 않았습니다. 사용자 질문과 매뉴얼 내용을 기준으로 안내하되, 현재 화면에 없는 버튼이나 정보는 추측하지 마세요.")
    
    return SCREEN_GUIDE.get(screen_id,(
        "현재 화면 정보를 찾지 못했습니다. 화면에 보이는 버튼과 사용자 질문을 기준으로만 안내하세요."
    ))
    
def get_manual_context(user_question):
    """ 
    사용자 질문과 관련된 매뉴얼 내용을 Chroma DB에서 검색한다.
    LLM 프롬프트에 넣기 좋은 짧은 문자열로 만든다.
    """
    if not user_question:
        return "사용자 질문이 없어 매뉴얼을 검색하지 못했습니다."
    retriever=get_retriever()
    docs=retriever.invoke(user_question)

    if not docs:
        return "관련 매뉴얼 내용을 찾지 못했습니다."
    
    result=[]
    
    for index, doc in enumerate(docs[:3],start=1):
        #LLM 참고자료가 길어지지 않도록 앞부분만 사용
        content=doc.page_content[:500]
        result.append(f"[검색결과 {index}]\n{content}")
    return "\n\n".join(result)

# 대화 내용 가져오기
def get_history(session_id):
    messages=get_messages(session_id)
    if not messages:
        return "이전 대화 없음"
    return format_messages(messages)

# 요금 질문 확인
def is_fee_policy_question(user_question)->bool:
    return detect_fee_type(user_question)!= "unknown"

# 요금 정책 가져오기
def get_fee_context(user_question)->str:
    """
    요금/할인/무료시간/회차/결제 후 출차 가능 시간 관련 질문이면
    요금 정책 tool을 실행해서 결과를 가져온다.
    관련 없는 질문이면 기본 문구를 반환한다.
    """
    query_type=detect_fee_type(user_question)
    if query_type=="unknown":
        return "요금/정책 DB조회가 필요하지 않은 질문입니다."
    return get_fee_policy.invoke({"user_question":user_question})

# 프롬프트 조합하기
def build_prompt(session_id,user_question,screen_id=None):
    history=get_history(session_id)
    screen_guide=get_screen_guide(screen_id)
    manual_context=get_manual_context(user_question)
    
    return KIOSK_USER_PROMPT_TEMPLATE.format(
        screen_id=screen_id or "unknown",
        screen_guide=screen_guide,
        chat_history=history,
        manual_context=manual_context,
        user_question=user_question
    )
    
# llm 호출
def get_llm():
    api_key=os.getenv("OPENAI_API_KEY")
    return ChatOpenAI(
        model="gpt-4o-mini",
        temperature=0,
        api_key=api_key
    )

# 챗봇 응답형식 통일(reply:일반답변/navigate:화면이동)
def make_chat_response(answer,action="reply",target_path=None, target_screen_id=None):
    return{
        "answer":answer,
        "action":action,
        "target_path":target_path,
        "target_screen_id":target_screen_id
    }
   
# 백엔드 호출 시
def chat(session_id,user_question,screen_id=None):
    #사용자 질문 저장
    save_message(session_id,"user",user_question)

    #키오스크와 관련 없는 질문인 경우
    if is_out_of_scope_question(user_question,screen_id):
        answer =  (
            "주차장 키오스크 이용과 관련된 질문만 안내할 수 있습니다. "
            "차량 찾기, 사전 정산, 결제, 할인권, 요금 정책에 대해 물어봐 주세요."
        )
        save_message(session_id, "assistant", answer)
        return make_chat_response(answer,"reply",None,None)

    #화면 이동 요청인지 확인
    navigate_response=get_navigation_response(user_question,screen_id)
    if navigate_response:
        save_message(session_id,"assistant",navigate_response["answer"])
        return navigate_response

    # 화면 이동 요청이 아닌 경우
    # prompt만들기
    prompt=build_prompt(session_id,user_question,screen_id)
    #llm호출
    tools=[get_fee_policy]
    tool_map={tool.name:tool for tool in tools}
    llm=get_llm().bind_tools(tools)
    messages=[SystemMessage(content=KIOSK_SYSTEM_PROMPT), HumanMessage(content=prompt)]
    response=llm.invoke(messages)

    # llm이 tool을 호출한 경우
    if response.tool_calls:
        messages.append(AIMessage(content=response.content, tool_calls=response.tool_calls))
        for tool_call in response.tool_calls:
            tool=tool_map[tool_call["name"]]
            tool_result=tool.invoke(tool_call["args"])
            messages.append(ToolMessage(content=tool_result, tool_call_id=tool_call["id"]))
        final_result=llm.invoke(messages)

    # llm이 tool을 호출하지 않았지만 요금 질문인 경우(fallback)
    elif is_fee_policy_question(user_question):
        history=get_history(session_id)
        fee_context=get_fee_context(user_question)

        get_fee_prompt=KIOSK_FEE_PROMPT_TEMPLATE.format(
            user_question=user_question,
            fee_context=fee_context,
            chat_history=history
        )
        final_result=llm.invoke([SystemMessage(content=KIOSK_SYSTEM_PROMPT), HumanMessage(content=get_fee_prompt)])
    else: 
        final_result=response

    answer=final_result.content
    #챗봇 답변 저장
    save_message(session_id,"assistant",answer)
    return make_chat_response(answer,"reply",None,None)

#질문 키워드  true면 차단할 질문
def is_out_of_scope_question(user_question, screen_id=None)->bool:
    if not user_question:
        return True
    
    kiosk_keywords=[
        "주차", "차량", "차랑번호", "차번호", "차번", "번호", "번호판",
        "결제", "정산", "요금", "할인", "할인권", "정책", "방문객"
        "QR", "계좌", "휴대폰", "상가",
        "검색", "지우기", "초기화", "관리자",
        "위치", "내 차", "출차", "입차",
        "버튼", "화면", "키오스크",
    ]
    contextual_keywords = [
        "여기서", "지금","이 화면","이거","다음",
        "어떻게", "뭐 눌러","뭘 눌러","어디 눌러","어디",
        "안돼","안 돼","오류","지우기","초기화","돌아가기","홈"
    ]
    # 키오스크 관련 단어가 있으면 허용
    if any(contains_keyword(user_question,keyword) for keyword in kiosk_keywords):
        return False
    
    # 현재 화면이 있고, 화면 맥락형 질문이면 허용
    if screen_id and any(contains_keyword(user_question,keyword) for keyword in contextual_keywords):
        return False
    
    # 여기까지 못 걸렸으면 키오스크 범위 밖 질문
    return True

if __name__ == "__main__":
    tests = [
        "외부인 요금 알려줘",
        "방문객 요금 알려줘",
        "할인권 종류 알려줘",
        "결제 후 몇 분 안에 나가야 해?",
        "차량번호를 잘못 입력했어요",
    ]

    for question in tests:
        print("질문:", question)

        result = chat(
            session_id="test-fee-context",
            user_question=question,
            screen_id="pay_input"
        )

        print(result)
        print("-" * 60)