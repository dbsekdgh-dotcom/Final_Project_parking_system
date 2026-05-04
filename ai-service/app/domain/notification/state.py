from typing import TypedDict

class NotificationState(TypedDict):
    notification_type: str
    context: dict
    title: str
    content: str