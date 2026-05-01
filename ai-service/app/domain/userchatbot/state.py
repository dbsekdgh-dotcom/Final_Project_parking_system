from typing import Annotated, TypedDict, Union, List
from langgraph.graph.message import add_messages

class ChatbotState(TypedDict):

    messages: Annotated[list, add_messages]

    access_token: str

    refresh_token: str

    context: dict
