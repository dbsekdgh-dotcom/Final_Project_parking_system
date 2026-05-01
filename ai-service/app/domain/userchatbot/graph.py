from langgraph.graph import StateGraph, END
from langgraph.prebuilt import ToolNode, tools_condition

from .state import ChatbotState
from .node import assistant_node, tools

workflow = StateGraph(ChatbotState)

workflow.add_node("assistant", assistant_node)
workflow.add_node("tools", ToolNode(tools))

workflow.set_entry_point("assistant")

workflow.add_conditional_edges(
    "assistant",
    tools_condition,
)

workflow.add_edge("tools", "assistant")

app_graph = workflow.compile()