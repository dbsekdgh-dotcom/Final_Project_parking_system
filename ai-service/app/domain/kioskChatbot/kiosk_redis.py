from dotenv import load_dotenv
from langchain_community.chat_message_histories import RedisChatMessageHistory
import os
load_dotenv()

# 서버 이중화 및 TTL 설정을 위해서  redis에 저장
REDIS_HOST = os.getenv("REDIS_HOST", "localhost")                                                                                                                                                                                                                                                  
REDIS_PORT = int(os.getenv("REDIS_PORT", "6379"))   
REDIS_URL=f"redis://{REDIS_HOST}:{REDIS_PORT}/0"

KEY_PREFIX="chat:"
# 저장할 시간 및 대화 갯수
TTL_SECONDS=600
MAX_MESSAGES=10

# 대화 내용 저장소 객체 꺼내오기
def get_history(session_id):
   return RedisChatMessageHistory(
       session_id=session_id,
       url=REDIS_URL,
       key_prefix=KEY_PREFIX,
       ttl=TTL_SECONDS
   )

# 대화내용 꺼내오기
def get_messages(session_id):
    history=get_history(session_id)
    return history.messages[-MAX_MESSAGES:]


# 대화 내용 저장하기
def save_message(session_id,role,content):
    history=get_history(session_id)
    
    if role=="user":
        history.add_user_message(content)
    elif role=="assistant":
        history.add_ai_message(content)

        
# 화면에 보여줄 내용으로 변환하기
def format_messages(messages):
    dialog=[]
    for m in messages:
        if m.type=="human":
            dialog.append(f"사용자:{m.content}")
        elif m.type=="ai":
            dialog.append(f"챗봇:{m.content}")
    # 문자열을 줄바꿈으로 연결해서 하나의 문자열로 변환
    return "\n".join(dialog)

# 대화기록 삭제
def clear_messages(session_id):
    history=get_history(session_id)
    history.clear()