from prompts.kiosk_chatbot_prompt import KIOSK_SYSTEM_PROMPT,KIOSK_USER_PROMPT_TEMPLATE,SCREEN_GUIDE
from prompts.kiosk_navigation import STORE_LOGGED_IN_SCREEN_IDS
from app.domain.kioskChatbot.kiosk_vectorstore import get_retriever
from app.domain.kioskChatbot.kiosk_redis import get_messages,format_messages,save_message
import os
from langchain_openai import ChatOpenAI
from langchain_core.messages import SystemMessage, HumanMessage

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

# 프롬프트 조합하기
def build_prompt(session_id,user_question,screen_id=None):
    screen_guide=get_screen_guide(screen_id)
    history=get_history(session_id)
    manual_context=get_manual_context(user_question)
    return KIOSK_USER_PROMPT_TEMPLATE.format(
        screen_id=screen_id,
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
   
# 백엔드 호출 시
def chat(session_id,user_question,screen_id=None):
    #사용자 질문 저장
    save_message(session_id,"user",user_question)

    if question_keyword(user_question):
        answer =  (
            "주차장 키오스크 이용과 관련된 질문만 안내할 수 있습니다. "
            "차량 찾기, 사전 정산, 결제, 할인권, 관리자 호출에 대해 물어봐 주세요."
        )
        save_message(session_id, "assistant", answer)
        return answer

    # prompt만들기
    prompt=build_prompt(session_id,user_question,screen_id)
    #llm호출
    llm=get_llm()
    response=llm.invoke([
        SystemMessage(content=KIOSK_SYSTEM_PROMPT),
        HumanMessage(content=prompt)
    ])
    answer=response.content
    #챗봇 답변 저장
    save_message(session_id,"assistant",answer)
    return answer
    # return {
    #     "answer":answer,
    #     "action":"reply",
    #     "target_path":None,
    #     "target_screen_id": None
    # }

#질문 키워드 
def question_keyword(user_question)->bool:
    if not user_question:
        return True
    
    kiosk_keywords=[
        "주차", "차량", "차", "번호", "번호판",
        "결제", "정산", "요금", "할인", "할인권",
        "QR", "계좌", "휴대폰", "상가",
        "검색", "지우기", "초기화", "관리자",
        "위치", "내 차", "출차", "입차",
        "버튼", "화면", "키오스크",
        "여기서","어떻게"
    ]
    return not any(keyword in user_question for keyword in kiosk_keywords)


#상가 로그인 여부 
def is_store_login(screen_id=None,store_login=None):
    """
    상가 로그인 여부 판단.
    store_logged_in 값이 명확하게 들어오면 그 값을 우선 사용한다.
    아니면 현재 screen_id로 대략 판단한다.
    """
    if store_login is not None:
        return store_login
    return screen_id in STORE_LOGGED_IN_SCREEN_IDS