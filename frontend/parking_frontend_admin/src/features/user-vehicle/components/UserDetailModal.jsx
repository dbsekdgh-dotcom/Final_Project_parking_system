import ManagementModal from './ManagementModal.jsx';
import { Row, formatDate } from './ManagementHelpers.jsx';

export default function UserDetailModal({ data, onClose }) {
  return (
    <ManagementModal title="사용자 상세" onClose={onClose}>
      <div className="mgmt-modal__section">
        <div className="mgmt-modal__section-title">기본 정보</div>
        <Row label="이름"     value={data.name} />
        <Row label="이메일"   value={data.email} />
        <Row label="전화번호" value={data.phone} />
        <Row label="생년월일" value={data.birth} />
        <Row label="가입일"   value={formatDate(data.createdAt)} />
        <Row label="상태"     value={data.status === 'ACTIVE' ? '활성' : '탈퇴'} />
        {data.deletedAt && <Row label="탈퇴일" value={formatDate(data.deletedAt)} />}
      </div>

      {data.householdId && (
        <div className="mgmt-modal__section">
          <div className="mgmt-modal__section-title">입주민 정보</div>
          <Row label="세대번호"  value={data.unitNo} />
          <Row label="세대 상태" value={data.householdStatus} />
        </div>
      )}

      <div className="mgmt-modal__section">
        <div className="mgmt-modal__section-title">보유 차량</div>
        {data.vehicles?.length === 0 ? (
          <p style={{ color: '#6b7280', fontSize: 13 }}>등록된 차량이 없습니다.</p>
        ) : (
          data.vehicles?.map(v => (
            <div key={v.vehicleId} className="mgmt-modal__row">
              <span className="mgmt-modal__label">{v.carNumber}</span>
              <span className="mgmt-modal__value">{v.vehicleName} · {v.status}</span>
            </div>
          ))
        )}
      </div>
    </ManagementModal>
  );
}
