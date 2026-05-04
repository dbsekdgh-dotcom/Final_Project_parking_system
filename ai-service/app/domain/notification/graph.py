from langgraph.graph import StateGraph, END, START
from .state import NotificationState
from .nodes import(
    vehicle_approved_node,
    vehicle_rejected_node,
    reservation_approved_node,
    reservation_rejected_node,
    resident_approved_node,
    route_by_type,
)

workflow = StateGraph(NotificationState)

# 타입별 노드 등록
workflow.add_node("VEHICLE_APPROVED", vehicle_approved_node)
workflow.add_node("VEHICLE_REJECTED", vehicle_rejected_node)
workflow.add_node("RESIDENT_APPROVED", resident_approved_node)
workflow.add_node("RESERVATION_APPROVED", reservation_approved_node)
workflow.add_node("RESERVATION_REJECTED", reservation_rejected_node)

# START에서 notification_type 값으로 분기
workflow.add_conditional_edges(
    START,
    route_by_type,
    {
        "VEHICLE_APPROVED" : "VEHICLE_APPROVED",
        "VEHICLE_REJECTED" : "VEHICLE_REJECTED",
        "RESIDENT_APPROVED" : "RESIDENT_APPROVED",
        "RESERVATION_APPROVED" : "RESERVATION_APPROVED",
        "RESERVATION_REJECTED" : "RESERVATION_REJECTED",
    },
)

# 모든 노드 -> END
for node in ["VEHICLE_APPROVED", "VEHICLE_REJECTED", "RESIDENT_APPROVED",
             "RESERVATION_APPROVED","RESERVATION_REJECTED"]:
    workflow.add_edge(node, END)

notification_graph = workflow.compile()