import ManagementModal from './ManagementModal.jsx';
import { Row, formatDate, formatDateTime } from './ManagementHelpers.jsx';

const STATUS_LABEL = {
  ACTIVE:    '활성',
  EXPIRED:   '만료',
  REFUNDED:  '환불',
  CANCELLED: '취소',
};

const PAYMENT_STATUS_LABEL = {
  PAID:     '결제완료',
  REFUNDED: '환불',
  PENDING:  '대기',
};

export default function SubscriptionDetailModal({ data, onClose }) {
  return (
    <ManagementModal title="정기권 상세" onClose={onClose}>
      <div className="mgmt-modal__section">
        <div className="mgmt-modal__section-title">정기권 정보</div>
        <Row label="상태"   value={STATUS_LABEL[data.status] ?? data.status} />
        <Row label="시작일" value={formatDate(data.startDate)} />
        <Row label="만료일" value={formatDate(data.endDate)} />
        <Row label="가격"   value={data.price != null ? `${data.price.toLocaleString()}원` : '-'} />
        <Row label="구매일" value={formatDate(data.createdAt)} />
        {data.activatedAt && <Row label="활성화일" value={formatDateTime(data.activatedAt)} />}
        {data.cancelledAt && <Row label="취소일"   value={formatDateTime(data.cancelledAt)} />}
      </div>

      <div className="mgmt-modal__section">
        <div className="mgmt-modal__section-title">결제 정보</div>
        <Row label="결제금액" value={data.paymentAmount != null ? `${data.paymentAmount.toLocaleString()}원` : '-'} />
        <Row label="결제수단" value={data.paymentMethod} />
        <Row label="결제상태" value={PAYMENT_STATUS_LABEL[data.paymentStatus] ?? data.paymentStatus} />
        <Row label="결제일"   value={formatDateTime(data.paidAt)} />
        {data.refundedAmount != null && (
          <Row label="환불금액" value={`${data.refundedAmount.toLocaleString()}원`} />
        )}
      </div>

      <div className="mgmt-modal__section">
        <div className="mgmt-modal__section-title">차량 / 소유자</div>
        <Row label="차량번호" value={data.carNumber} />
        <Row label="차량명"   value={data.vehicleName} />
        <Row label="소유자"   value={data.ownerName} />
      </div>
    </ManagementModal>
  );
}
