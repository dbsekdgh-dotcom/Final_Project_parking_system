import React from 'react';

  // activityType → 관련 ID 필드 매핑
  const RELATED_ID_MAP = {
      ENTRY:                 'parkingLogId',
      EXIT:                  'parkingLogId',
      ADMIN_FORCE_EXIT:      'parkingLogId',
      PAYMENT_PRE:           'paymentId',
      PAYMENT_EXIT:          'paymentId',
      PAYMENT_CANCEL:        'paymentId',
      REFUNDED:              'paymentId',
      RESERVATION_CREATED:   'reservationId',
      RESERVATION_CANCELLED: 'reservationId',
  };

  const RELATED_LABEL_MAP = {
      parkingLogId:   '주차 로그 ID',
      paymentId:      '결제 ID',
      reservationId:  '예약 ID',
  };

  function Row({ label, value }) {
      if (value == null) return null;
      return (
          <div className="aal-modal__row">
              <span className="aal-modal__row-label">{label}</span>
              <span className="aal-modal__row-value">{value}</span>
          </div>
      );
  }

  export default function ActivityLogDetailModal({ log, onClose }) {
      const relatedField = RELATED_ID_MAP[log.activityType];
      const relatedId    = relatedField ? log[relatedField] : null;
      const relatedLabel = relatedField ? RELATED_LABEL_MAP[relatedField] : null;

      return (
          <div className="aal-modal__overlay" onClick={onClose}>
              <div className="aal-modal__box" onClick={e => e.stopPropagation()}>
                  <div className="aal-modal__header">
                      <span className="aal-modal__title">사용자 활동 상세</span>
                      <button className="aal-modal__close" onClick={onClose}>✕</button>
                  </div>
                  <div className="aal-modal__body">
                      <Row label="로그 ID"   value={log.activityId} />
                      <Row label="활동 유형" value={log.activityTypeLabel} />
                      <Row label="발생 일시" value={formatDateTime(log.createdAt)} />
                      <Row label="메시지"   value={log.message} />
                      <div className="aal-modal__divider" />
                      <Row label="사용자"   value={log.userName ?? '비회원'} />
                      <Row label="연락처"   value={log.userPhone} />
                      <Row label="동호수"   value={log.unitNo != null ? `${log.unitNo}호` : null} />
                      <Row label="차량번호" value={log.carNumber} />
                      {relatedId != null && (
                          <>
                              <div className="aal-modal__divider" />
                              <Row label={relatedLabel} value={relatedId} />
                          </>
                      )}
                  </div>
                  <div className="aal-modal__footer">
                      <button className="aal-modal__btn-cancel" onClick={onClose}>닫기</button>
                  </div>
              </div>
          </div>
      );
  }

  function formatDateTime(isoStr) {
      if (!isoStr) return '–';
      const d = new Date(isoStr);
      return `${d.getFullYear()}/${String(d.getMonth()+1).padStart(2,'0')}/${String(d.getDate()).padStart(2,'0')}
  ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`;
  }