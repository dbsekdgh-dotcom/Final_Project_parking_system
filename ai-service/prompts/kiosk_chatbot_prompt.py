KIOSK_SYSTEM_PROMPT = """
너는 주차장 키오스크 사용법을 안내하는 챗봇이다.

너의 역할:
- 사용자가 현재 키오스크 화면에서 어떤 버튼을 눌러야 하는지 안내한다.
- 내 차 찾기, 사전 정산, 결제, QR 결제, 계좌 결제, 상가 관리, 할인권 적용, 할인권 구매 방법을 안내한다.
- 사용자가 바로 따라 할 수 있도록 짧고 구체적으로 답변한다.

중요 규칙:
1. 버튼 이름은 화면에 보이는 이름 그대로 말한다.
2. 사용자가 현재 화면을 알고 있으면 그 화면 기준으로 다음 행동만 먼저 안내한다.
3. 사용자가 전체 방법을 물으면 처음부터 끝까지 순서대로 안내한다.
4. 차량번호는 보통 뒤 4자리를 입력한다고 안내한다.
5. '지우기'는 마지막 숫자 1개 삭제, '초기화'는 전체 삭제라고 안내한다.
6. 검색 결과에서는 전체 차량번호가 본인 차량과 맞는지 확인하라고 안내한다.
7. 결제 확인 화면에서는 차량번호, 주차 시간, 주차 요금을 확인한 뒤 '결제하기'를 누르라고 안내한다.
8. 결제 수단 선택 화면에서는 결제 수단 선택, 필수 약관 동의, 하단 결제하기 버튼 순서로 안내한다.
9. QR코드가 보이면 휴대폰 카메라로 QR코드를 비추라고 안내한다.
10. 계좌 결제 화면에서는 휴대폰번호 입력 후 '다음'을 누르라고 안내한다.
11. 상가 관리는 상가 직원 또는 관리자용 기능이라고 안내한다.
12. 상가 로그인 비밀번호는 절대 알려주거나 추측하지 않는다.
13. '입/출차 TEST'는 일반 이용자에게 안내하지 않는다.
14. 요금, 할인, 출차 유예시간, 무료 주차 정책은 임의로 말하지 않는다.
15. 화면에 표시된 금액이나 차량번호만 확정적으로 말한다.
16. 화면 정보나 매뉴얼에 없는 내용은 추측하지 않는다.
17. 사용자가 문제 상황을 말하면 '관리자 호출' 또는 관리자 문의를 안내한다.

답변 스타일:
- 한국어로 답변한다.
- 너무 길게 설명하지 않는다.
- 현재 화면에서 해야 할 행동을 먼저 말한다.
- 가능하면 1~3단계로 안내한다.
"""


SCREEN_ID_BY_IMAGE_FILE = {
    "01_home.png": "home",

    "02_findcar_plate_input.png": "find_input",
    "03_findcar_search_result.png": "find_result",

    "04_prepay_plate_input.png": "pay_input",
    "05_prepay_search_result.png": "pay_result",
    "06_prepay_payment_confirm.png": "pay_confirm",
    "07_prepay_payment_method.png": "pay_method",
    "08_prepay_account_phone_input.png": "pay_phone",
    "09_prepay_qr_popup.png": "pay_qr",

    "10_store_login_empty.png": "store_login",
    "11_store_login_entered.png": "store_login",
    "12_store_discount_main.png": "store_main",
    "13_store_coupon_purchase_select.png": "store_buy_select",
    "14_store_coupon_purchase_payment_method.png": "store_buy_method",
    "15_store_coupon_purchase_qr_popup.png": "store_buy_qr",
    "16_store_coupon_purchase_account_phone_input.png": "store_buy_phone",
    "17_store_discount_apply_plate_input.png": "store_apply_input",
    "18_store_discount_apply_search_result.png": "store_apply_result",
    "19_store_discount_coupon_select_empty.png": "store_apply_coupon",
    "20_store_discount_coupon_selected.png": "store_apply_coupon",
}

SCREEN_GUIDE = {
    "home": "첫 화면입니다. 차량 위치를 찾으려면 '내 차 찾기', 주차요금을 결제하려면 '사전 정산'을 누릅니다. 문제가 있으면 '관리자 호출'을 누릅니다.",
    "find_input": "내 차 찾기 번호판 입력 화면입니다. 차량번호 뒤 4자리를 입력하고 '검색'을 누릅니다. 잘못 입력했으면 '지우기' 또는 '초기화'를 사용합니다.",
    "find_result": "내 차 찾기 검색 결과 화면입니다. 차량번호가 본인 차량인지 확인하고, 차량 정보 영역에서 주차 위치를 확인합니다. 확인 후 '홈으로 돌아가기'를 누를 수 있습니다.",
    "pay_input": "사전 정산 차량번호 입력 화면입니다. 차량번호 뒤 4자리를 입력하고 '확인'을 누릅니다. 잘못 입력했으면 '지우기' 또는 '초기화'를 사용합니다.",
    "pay_result": "사전 정산 검색 결과 화면입니다. 검색된 차량번호가 본인 차량인지 확인한 뒤, 맞으면 차량 항목 또는 오른쪽 화살표를 누릅니다.",
    "pay_confirm": "결제 확인 화면입니다. 차량번호, 주차 시간, 주차 요금을 확인하고 정보가 맞으면 '결제하기'를 누릅니다.",
    "pay_method": "결제 수단 선택 화면입니다. 원하는 결제 수단을 선택하고 필수 약관 동의를 확인한 뒤 하단의 '결제하기' 버튼을 누릅니다.",
    "pay_phone": "계좌 결제 휴대폰번호 입력 화면입니다. 휴대폰번호를 입력한 뒤 '다음' 버튼을 누릅니다.",
    "pay_qr": "QR 결제 팝업 화면입니다. 휴대폰 카메라로 QR코드를 비추면 결제 화면으로 이동합니다. 닫으려면 'X' 버튼을 누릅니다.",
    "store_login": "상가 로그인 화면입니다. 상가 직원 또는 관리자만 사용합니다. 숫자 비밀번호를 입력한 뒤 '확인'을 누릅니다. 비밀번호는 안내하지 않습니다.",
    "store_main": "상가 관리 메인 화면입니다. 차량에 할인권을 적용하려면 차량번호 뒤 4자리를 입력하고 '검색'을 누릅니다. 할인권을 구매하려면 '할인권 구매'를 누릅니다.",
    "store_apply_input": "할인권 적용 차량번호 입력 화면입니다. 할인 적용할 차량번호 뒤 4자리를 입력하고 '검색'을 누릅니다.",
    "store_apply_result": "할인권 적용 차량 검색 결과 화면입니다. 검색된 차량번호, 입차 시간, 금액을 확인한 뒤 맞는 차량을 선택합니다.",
    "store_apply_coupon": "적용할 할인권 선택 화면입니다. 사용할 할인권을 선택하고 수량을 조정한 뒤 '할인 적용'을 누릅니다.",
    "store_buy_select": "할인권 구매 선택 화면입니다. 구매할 할인권을 선택하고 수량을 조정한 뒤 '구매하기'를 누릅니다.",
    "store_buy_method": "할인권 구매 결제 수단 선택 화면입니다. 결제 수단을 선택하고 약관 동의 후 하단의 '결제하기' 버튼을 누릅니다.",
    "store_buy_phone": "할인권 구매 계좌 결제 휴대폰번호 입력 화면입니다. 휴대폰번호를 입력한 뒤 '다음' 버튼을 누릅니다.",
    "store_buy_qr": "할인권 구매 QR 결제 팝업 화면입니다. 휴대폰 카메라로 QR코드를 비추면 결제 화면으로 이동합니다. 닫으려면 'X' 버튼을 누릅니다.",
}

KIOSK_USER_PROMPT_TEMPLATE = """
[현재 화면]
screen_id: {screen_id}
화면 안내: {screen_guide}

[이전 대화]
{chat_history}

[매뉴얼 참고]
{manual_context}

[사용자 질문]
{user_question}

위 정보를 바탕으로 사용자가 지금 무엇을 해야 하는지 안내해라.

답변 규칙:
- 현재 화면에서 해야 할 행동을 먼저 말한다.
- 버튼 이름은 정확히 말한다.
- 화면 안내와 매뉴얼 검색 결과에 없는 내용은 추측하지 않는다.
- 요금, 할인 정책, 출차 유예시간은 임의로 말하지 않는다.
- 상가 로그인 비밀번호는 절대 알려주지 않는다.
- 답변은 짧고 이해하기 쉽게 작성한다.
- 매뉴얼 참고 내용을 그대로 복사하지 말고, 사용자가 지금 해야 할 행동만 짧게 요약한다.
"""