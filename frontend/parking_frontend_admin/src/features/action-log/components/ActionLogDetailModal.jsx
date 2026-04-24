import { TARGET_LABEL, ACTION_LABEL } from "../pages/ActionLogPage";

export default function ActionLogDetailModal({ log, onClose, onRevert}){
    const canRevert = log.actionType === 'UPDATE' && !log.isReverted;

    const handleRevert = () => {
        if(!window.confirm('이 작업을 되돌리시겠습니까?')) return;
        onRevert(log.actionId);
    };
     return (
      <div className="aal-modal__overlay" onClick={onClose}>
        <div className="aal-modal__box" onClick={e => e.stopPropagation()}>
          <div className="aal-modal__header">
            <span className="aal-modal__title">활동 상세</span>
            <button className="aal-modal__close" onClick={onClose}>✕</button>
          </div>

          <div className="aal-modal__body">
            <Row label="관리자"   value={log.adminName} />
            <Row label="대상 유형" value={TARGET_LABEL[log.targetType] ?? log.targetType} />
            <Row label="작업 유형" value={ACTION_LABEL[log.actionType] ?? log.actionType} />
            <Row label="대상 ID"  value={log.targetId ?? '–'} mono />
            <Row label="일시"     value={formatDateTime(log.createdAt)} mono />

            <div className="aal-modal__divider" />

            <p className="aal-modal__json-label">변경 전</p>
            <pre className="aal-modal__json">{formatJson(log.beforeData)}</pre>

            <p className="aal-modal__json-label">변경 후</p>
            <pre className="aal-modal__json">{formatJson(log.afterData)}</pre>

            {log.isReverted && (
              <>
                <div className="aal-modal__divider" />
                <Row label="되돌린 관리자" value={log.revertedByAdminName} />
                <Row label="되돌린 일시"   value={formatDateTime(log.revertedAt)} mono />
              </>
            )}
          </div>

          <div className="aal-modal__footer">
            <button className="aal-modal__btn-cancel" onClick={onClose}>닫기</button>
            {canRevert && (
              <button className="aal-modal__btn-revert" onClick={handleRevert}>되돌리기</button>
            )}
          </div>
        </div>
      </div>
    );
}

 function Row({ label, value, mono }) {
    return (
      <div className="aal-modal__row">
        <span className="aal-modal__row-label">{label}</span>
        <span className={`aal-modal__row-value${mono ? ' aal-modal__row-value--mono' : ''}`}>{value}</span>
      </div>
    );
  }

  function formatDateTime(isoStr) {
    if (!isoStr) return '–';
    const d = new Date(isoStr);
    return `${d.getFullYear()}/${String(d.getMonth()+1).padStart(2,'0')}/${String(d.getDate()).padStart(2,'0')}
  ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`;
  }

  function formatJson(jsonStr) {
    try { return JSON.stringify(JSON.parse(jsonStr), null, 2); }
    catch { return jsonStr ?? '–'; }
  }