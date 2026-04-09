// 객체 수정 방지를 위해 Object.freeze를 써주면 더 안전합니다.
export const PARKING_TYPE_LABELS = Object.freeze({
    RESIDENT: '입주민',
    VISIT: '외부인',
    USER: '회원',
    RESERVATION: '방문예약',
    SUBSCRIPTION: '정기권'
});

export const PARKING_STATUS_LABELS = Object.freeze({
    DETECTED: '입차시도',
    ENTRY_CANCELLED: '입차취소',
    ENTERED: '입차완료',
    EXIT_REQUESTED: '출차요청',
    EXITED: '출차완료',
    FORCE_EXITED: '강제출차'
});

export const PAYMENT_STATUS_LABELS = Object.freeze({
    NONE: '무료',
    UNPAID: '미납',
    PAID: '납부완료'
});