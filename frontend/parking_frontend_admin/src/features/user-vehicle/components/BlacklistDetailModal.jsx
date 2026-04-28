import { useState } from 'react';
import ManagementModal from './ManagementModal.jsx';
import { Row, formatDateTime } from './ManagementHelpers.jsx';
import { releaseBlacklist } from '../api/ManagementApi';

const REASON_LABEL = {
    REPORT_ACCUMULATION: '신고 누적',
    ILLEGAL_VEHICLE:     '불법 차량',
    USER_BLACKLIST:      '사용자 차단',
    ADMIN_MANUAL:        '관리자 직접 등록',
    SYSTEM_BLOCK:        '시스템 차단',
};

export default function BlacklistDetailModal({ data, onClose, onRefresh }) {
    const [releasing, setReleasing] = useState(false);

    const handleRelease = async () => {
        if (!window.confirm(`[${data.carNumber}] 차량의 차단을 해제하시겠습니까?`)) return;
        setReleasing(true);
        try {
            await releaseBlacklist(data.id);
            alert('차단이 해제되었습니다.');
            onRefresh();
            onClose();
        } catch {
            alert('해제 처리 중 오류가 발생했습니다.');
        } finally {
            setReleasing(false);
        }
    };

    return (
        <ManagementModal title="블랙리스트 상세" onClose={onClose}>

            <div className="mgmt-modal__section">
                <div className="mgmt-modal__section-title">차단 정보</div>
                <Row label="차량번호" value={data.carNumber} />
                <Row label="차단 사유" value={REASON_LABEL[data.reasonType] ?? data.reasonType} />
                <Row label="상세 사유" value={data.reasonDetail || '-'} />
                <Row label="차단 시작" value={formatDateTime(data.startDate)} />
                <Row label="차단 종료" value={data.permanent ? '영구' : formatDateTime(data.endDate)} />
                <Row label="상태" value={
                    <span className={`arp__status-badge arp__status-badge--${data.status.toLowerCase()}`}>
                        {data.status === 'ACTIVE' ? '차단중' : '해제'}
                    </span>
                } />
                <Row label="등록일" value={formatDateTime(data.createdAt)} />
                {data.releasedAt && <Row label="해제일" value={formatDateTime(data.releasedAt)} />}
            </div>

            {data.vehicleId && (
                <div className="mgmt-modal__section">
                    <div className="mgmt-modal__section-title">연결 차량 정보</div>
                    <Row label="차량명"   value={data.vehicleName || '-'} />
                    <Row label="소유자"   value={data.ownerName   || '-'} />
                    <Row label="이메일"   value={data.ownerEmail  || '-'} />
                </div>
            )}

            {!data.vehicleId && (
                <div className="mgmt-modal__section">
                    <div className="mgmt-modal__section-title">연결 차량 정보</div>
                    <p style={{ color: '#6b7280', fontSize: 13 }}>미등록 차량 (비회원)</p>
                </div>
            )}

            {data.status === 'ACTIVE' && (
                <button
                    style={{
                        marginTop: 4,
                        padding: '9px 0',
                        background: '#7f1d1d',
                        color: '#fca5a5',
                        border: '1px solid #991b1b',
                        borderRadius: 8,
                        cursor: 'pointer',
                        fontWeight: 600,
                        fontSize: 13,
                        width: '100%',
                    }}
                    onClick={handleRelease}
                    disabled={releasing}
                >
                    {releasing ? '처리 중...' : '차단 해제'}
                </button>
            )}
        </ManagementModal>
    );
}
