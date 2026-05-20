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
    "할래"
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

def has_navigation_intent(user_question)->bool:
    """ 
    사용자가 화면이동을 원하는지 확인
    """
    if not user_question:
        return False
    
    return any(contains_keyword(user_question,word) for word in NAVIGATION_INTENT_WORDS)

def is_explain_request(user_question)->bool:
    """ 
    사용자가 화면이동이 아니라 설명을 원하는지 확인
    """
    if not user_question:
        return False
    return any(contains_keyword(user_question,word)for word in EXPLAIN_WORDS)

def is_store_logged_in(screen_id=None,store_logged_in=None)->bool:
    """ 
    상가 로그인 여부 판단
    store_logged_id값 우선, 없으면 screen_id로
    """
    if store_logged_in is not None:
        return store_logged_in
    return screen_id in STORE_LOGGED_IN_SCREEN_IDS

def get_navigation_response(user_question,screen_id=None,store_logged_in=None):
    """ 
    사용자가 화면 이동을 요청한 경우
    
    이동 조건:
    1. 이동 의도 단어가 있어야 한다.
    2. 이동 대상 키워드가 있어야 한다.
    3. 설명 요청만 하는 경우는 이동하지 않는다.
    """
    if not user_question:
        return None
    
    #설명요청이고 이동요청이 아닌 경우
    if is_explain_request(user_question) and not has_navigation_intent(user_question):
        return None
    
    #이동 요청이 아닌경우
    if not  has_navigation_intent(user_question):
        return None
    
    for target in NAVIGATION_TARGETS:
        #user_question에 화면 키워드가 있는지
        match=any(contains_keyword(user_question,keyword) for keyword in target["keywords"])
        
        if not match:
            continue
        
        #상가 로그인이 필요한 화면이고 로그인이 되어있지 않은 경우 
        if target["requires_store_login"] and not is_store_logged_in(screen_id,store_logged_in):
            return{
                "answer":(
                    f"'{target["name"]}' 기능은 상가직원 로그인 후 사용할 수 있습니다. "
                    "먼저 상가 로그인 화면으로 이동하겠습니다."
                ),
                "action":"navigate",
                "target_path":"/store/login",
                "target_screen_id":"store_login"
            }
        return{
            "answer":target["answer"],
            "action":"navigate",
            "target_path":target["target_path"],
            "target_screen_id":target["target_screen_id"]
        }
    return None

def normalize_text(text)->str:
    """ 
    띄어쓰기 차이때문에 매칭이 실패하지 않도록 공백 제거
    """
    if not text:
        return ""
    return text.replace(" ","").strip()

def contains_keyword(user_question,keyword)->bool:
    """ 
    사용자 질문안에 keyword가 있는지 확인
    """
    normalize_question=normalize_text(user_question)
    normalize_keyword=normalize_text(keyword)
    return normalize_keyword in normalize_question

