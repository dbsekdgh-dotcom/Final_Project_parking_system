from prompts.kiosk_chatbot_prompt import KIOSK_SYSTEM_PROMPT,KIOSK_USER_PROMPT_TEMPLATE,SCREEN_GUIDE
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
    # prompt만들기
    prompt=build_prompt(session_id,user_question,screen_id)
    #사용자 질문 저장
    save_message(session_id,"user",user_question)
    #llm호출
    llm=get_llm()
    response=llm.invoke([
        SystemMessage(content=KIOSK_SYSTEM_PROMPT),
        HumanMessage(content=prompt)
    ])
    res=response.content
    #챗봇 답변 저장
    save_message(session_id,"assistant",res)
    return res

# if __name__ == "__main__":
#     answer = chat(
#         session_id="test-kiosk-001",
#         user_question="차량번호를 잘못 입력했어요",
#         screen_id="pay_input",
#     )

#     print("===== 챗봇 답변 =====")
#     print(answer)