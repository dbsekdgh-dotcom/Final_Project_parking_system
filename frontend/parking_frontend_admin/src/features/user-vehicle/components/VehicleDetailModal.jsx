import ManagementModal from './ManagementModal.jsx';
import { Row, formatDate } from './ManagementHelpers.jsx';

const STATUS_LABEL = {
  ACTIVE:  '활성',
  PENDING: '대기',
  DELETED: '삭제',
};

const HISTORY_LABEL = {
  APPROVED: '승인',
  REJECTED: '거절',
  PENDING:  '대기',
};

export default function VehicleDetailModal({ data, onClose }) {
  return (
    <ManagementModal title="차량 상세" onClose={onClose}>
      <div className="mgmt-modal__section">
        <div className="mgmt-modal__section-title">차량 정보</div>
        <Row label="차량번호" value={data.carNumber} />
        <Row label="차량명"   value={data.vehicleName} />
        <Row label="상태"     value={STATUS_LABEL[data.status] ?? data.status} />
        <Row label="등록일"   value={formatDate(data.createdAt)} />
        {data.deletedAt && <Row label="삭제일" value={formatDate(data.deletedAt)} />}
        {data.hasReRegistration && (
          <div className="mgmt-modal__row">
            <span className="mgmt-modal__label">재등록</span>
            <span className="mgmt-modal__re-badge">타인 재등록 이력 있음</span>
          </div>
        )}
      </div>

      <div className="mgmt-modal__section">
        <div className="mgmt-modal__section-title">소유자 정보</div>
        <Row label="이름"   value={data.ownerName} />
        <Row label="이메일" value={data.ownerEmail} />
      </div>

      {data.registrationHistory?.length > 0 && (
        <div className="mgmt-modal__section">
          <div className="mgmt-modal__section-title">등록 승인 이력</div>
          <div className="mgmt-modal__history">
            {data.registrationHistory.map((h, i) => (
              <div key={i} className="mgmt-modal__row">
                <span className="mgmt-modal__label">
                  {h.requesterName} · {formatDate(h.appliedAt)}
                </span>
                <span className="mgmt-modal__value">
                  {HISTORY_LABEL[h.result] ?? h.result}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}
    </ManagementModal>
  );
}
