NAVIGATION_TARGETS = [
    {
        "name": "내 차 찾기",
        "keywords": ["내 차 찾기", "차 위치", "차 어디", "내 차 어디", "차량 위치"],
        "answer": "'내 차 찾기' 화면으로 이동할게요. 차량번호 뒤 4자리를 입력하고 '검색'을 누르세요.",
        "target_path": "/find-car",
        "target_screen_id": "find_input",
        "requires_store_login": False,
    },
    {
        "name": "사전 정산",
        "keywords": ["사전 정산", "주차요금 결제", "요금 결제", "정산", "결제"],
        "answer": "'사전 정산' 화면으로 이동할게요. 차량번호 뒤 4자리를 입력하고 '확인'을 누르세요.",
        "target_path": "/prepayment",
        "target_screen_id": "pay_input",
        "requires_store_login": False,
    },
    {
        "name": "상가 관리",
        "keywords": ["상가 관리", "상가 로그인", "상가"],
        "answer": "'상가 로그인' 화면으로 이동할게요. 상가 직원 또는 관리자만 사용할 수 있습니다.",
        "target_path": "/store/login",
        "target_screen_id": "store_login",
        "requires_store_login": False,
    },
    {
        "name": "할인권 구매",
        "keywords": ["할인권 구매", "할인권 사기", "할인권 결제"],
        "answer": "'할인권 구매' 화면으로 이동할게요. 구매할 할인권을 선택하고 수량을 조정한 뒤 '구매하기'를 누르세요.",
        "target_path": "/store/purchase",
        "target_screen_id": "store_buy_select",
        "requires_store_login": True,
    },
    {
        "name": "할인권 적용",
        "keywords": ["할인권 적용", "할인 적용", "차량 할인"],
        "answer": "'할인권 적용' 화면으로 이동할게요. 차량번호 뒤 4자리를 입력하고 '검색'을 누르세요.",
        "target_path": "/store/apply",
        "target_screen_id": "store_apply_input",
        "requires_store_login": True,
    },
]


NAVIGATION_INTENT_WORDS = [
    "이동",
    "가줘",
    "가고 싶",
    "가고싶",
    "넘어가",
    "열어줘",
    "열어",
    "보여줘",
    "띄워줘",
    "시작",
    "하고 싶",
    "하고싶",
    "화면으로",
]


EXPLAIN_WORDS = [
    "방법",
    "어떻게",
    "설명",
    "알려줘",
    "알려",
    "뭐야",
    "무엇",
]

STORE_LOGGED_IN_SCREEN_IDS = {
    "store_main",
    "store_buy_select",
    "store_apply_input"
}

