import ManagementModal from './ManagementModal.jsx';
import { Row, formatDate, formatDateTime } from './ManagementHelpers.jsx';

const STATUS_LABEL = {
  RESERVED:  '예약',
  ENTERED:   '입차',
  COMPLETED: '완료',
  CANCELLED: '취소',
  REJECTED:  '거절',
  NO_SHOW:   '노쇼',
};

export default function ReservationDetailModal({ data, onClose }) {
  return (
    <ManagementModal title="방문예약 상세" onClose={onClose}>
      <div className="mgmt-modal__section">
        <div className="mgmt-modal__section-title">예약 정보</div>
        <Row label="차량번호"  value={data.carNumber} />
        <Row label="상태"      value={STATUS_LABEL[data.status] ?? data.status} />
        <Row label="방문 시작" value={formatDateTime(data.visitStartAt)} />
        <Row label="방문 종료" value={formatDateTime(data.visitEndAt)} />
        <Row label="신청일"    value={formatDate(data.createdAt)} />
        <Row label="무료 여부" value={data.isFree ? '무료' : '유료'} />
      </div>

      <div className="mgmt-modal__section">
        <div className="mgmt-modal__section-title">신청자 정보</div>
        <Row label="이름"     value={data.requesterName} />
        <Row label="전화번호" value={data.requesterPhone} />
        {data.unitNo && <Row label="세대번호" value={data.unitNo} />}
      </div>
    </ManagementModal>
  );
}
