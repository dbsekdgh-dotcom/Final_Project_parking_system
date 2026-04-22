import ManagementModal from './ManagementModal.jsx';
import { Row, formatDate, formatDateTime } from './ManagementHelpers.jsx';

const STATUS_LABEL = {
  ACTIVE:   '활성',
  EXPIRED:  '만료',
  REFUNDED: '환불',
  CANCELLED:'취소',
};

export default function SubscriptionDetailModal({ data, onClose }) {
  return (
    <ManagementModal title="정기권 상세" onClose={onClose}>
      <div className="mgmt-modal__section">
        <div className="mgmt-modal__section-title">정기권 정보</div>
        <Row label="상태"      value={STATUS_LABEL[data.status] ?? data.status} />
        <Row label="시작일"    value={formatDate(data.startDate)} />
        <Row label="만료일"    value={formatDate(data.endDate)} />
        <Row label="결제금액"  value={data.amount != null ? `${data.amount.toLocaleString()}원` : '-'} />
        <Row label="결제수단"  value={data.paymentMethod} />
        <Row label="구매일"    value={formatDate(data.createdAt)} />
        {data.refundedAt   && <Row label="환불일"    value={formatDateTime(data.refundedAt)} />}
        {data.cancelReason && <Row label="취소 사유" value={data.cancelReason} />}
      </div>

      <div className="mgmt-modal__section">
        <div className="mgmt-modal__section-title">차량 정보</div>
        <Row label="차량번호" value={data.carNumber} />
        <Row label="차량명"   value={data.vehicleName} />
      </div>

      <div className="mgmt-modal__section">
        <div className="mgmt-modal__section-title">소유자 정보</div>
        <Row label="이름"   value={data.ownerName} />
        <Row label="이메일" value={data.ownerEmail} />
      </div>
    </ManagementModal>
  );
}
