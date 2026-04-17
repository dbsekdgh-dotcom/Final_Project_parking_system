import './RejectModal.css';

/**
 * RejectModal — 거절/처리 사유 입력 공용 모달
 *
 * Props:
 *   title           {string}    - 모달 제목
 *   onClose         {fn}        - 닫기 핸들러
 *   onConfirm       {fn}        - 확정 핸들러
 *   confirmLabel    {string}    - 확정 버튼 텍스트 (기본: '확정')
 *   confirmVariant  {string}    - 'danger' | 'primary' (기본: 'danger')
 *   confirmDisabled {boolean}   - 확정 버튼 비활성화
 *   metaContent     {ReactNode} - 모달 상단 메타 정보 영역
 *   reasonLabel     {string}    - textarea 레이블 (기본: '사유')
 *   placeholder     {string}    - textarea placeholder
 *   value           {string}    - textarea 값
 *   onChange        {fn}        - (value: string) => void
 */
export default function RejectModal({
  title           = '사유 입력',
  onClose,
  onConfirm,
  confirmLabel    = '확정',
  confirmVariant  = 'danger',
  confirmDisabled = false,
  metaContent,
  reasonLabel     = '사유',
  placeholder     = '사유를 입력해 주세요...',
  value           = '',
  onChange,
}) {
  return (
    <div className="rm__overlay" onClick={onClose}>
      <div className="rm__modal" onClick={e => e.stopPropagation()}>

        <div className="rm__header">
          <h3>{title}</h3>
          <button className="rm__close" onClick={onClose}>✕</button>
        </div>

        <div className="rm__body">
          {metaContent && <div className="rm__meta">{metaContent}</div>}

          <label className="rm__label" htmlFor="rm-reason">
            {reasonLabel} <span className="rm__required">*</span>
          </label>
          <textarea
            id="rm-reason"
            className="rm__textarea"
            placeholder={placeholder}
            value={value}
            onChange={e => onChange?.(e.target.value)}
          />
        </div>

        <div className="rm__footer">
          <button className="rm__btn-cancel" onClick={onClose}>취소</button>
          <button
            className={`rm__btn-confirm rm__btn-confirm--${confirmVariant}`}
            onClick={onConfirm}
            disabled={confirmDisabled}
          >
            {confirmLabel}
          </button>
        </div>

      </div>
    </div>
  );
}
