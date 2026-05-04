import json
import yaml
from pathlib import Path
from langchain_openai import ChatOpenAI
from langchain_core.messages import HumanMessage
from .state import NotificationState
# 현재 nodes.py 경로 기준으로 프로젝트 루트 탐색
# parents[0] = notification ,[1]=domain, [2]=app, [3]=ai-service, [4]=Final_Project_Parking_System
_ROOT = Path(__file__).parents[4]
_PROMPT_DIR = _ROOT / "ai" / "prompts" / "notification"
_CONFIG_PATH = _ROOT / "ai" / "configs" / "config.yml"

# 프롬프트 파일명 매핑
PROMPT_MAP ={
    "VEHICLE_APPROVED": "vehicle_approved",
    "VEHICLE_REJECTED": "vehicle_rejected",
    "RESIDENT_APPROVED": "resident_approved",
    "RESERVATION_APPROVED": "reservation_approved",
    "RESERVATION_REJECTED": "reservation_rejected"
}

def _load_config() -> dict:
    with open(_CONFIG_PATH, encoding="utf-8") as f:
        return yaml.safe_load(f)

def _load_prompt(name:str) -> str:
    with open(_PROMPT_DIR / f"{name}.txt", encoding="utf-8") as f:
        return f.read()
    
def _get_model() -> ChatOpenAI:
    cfg = _load_config()
    return ChatOpenAI(
        model=cfg["model"],
        temperature=cfg["temperature"],
        max_tokens=cfg["max_tokens"]
    )
    
async def _generate(state: NotificationState, prompt_name:str) -> dict:
    """공통 LLM 호출 헬퍼, 프롬프트에 context를 채운 후 LLM 호출."""
    template = _load_prompt(prompt_name)
    filled = template.format(**state["context"])
    
    model = _get_model()
    response = await model.ainvoke([HumanMessage(content=filled)])
    
    try:
        result = json.loads(response.content)
        return {**state, "title": result["title"], "content":result["content"]}
    except (json.JSONDecodeError, KeyError):
        # JSON 파싱 실패시 원문 그대로 content에 넣음
        return {**state, "title": "알림", "content":response.content}

# 타입별 노드 ( 분기 목적지 )
async def vehicle_approved_node(state: NotificationState):
    return await _generate(state, "vehicle_approved")

async def vehicle_rejected_node(state: NotificationState):
    return await _generate(state, "vehicle_rejected")

async def resident_approved_node(state: NotificationState):
    return await _generate(state, "resident_approved")

async def reservation_approved_node(state: NotificationState):
    return await _generate(state, "reservation_approved")

async def reservation_rejected_node(state: NotificationState):
    return await _generate(state, "reservation_rejected")

# 라우팅 함수
def route_by_type(state: NotificationState) -> str:
    """notification_type 값을 그대로 반환해 해당 노드로 분기."""
    return state["notification_type"]