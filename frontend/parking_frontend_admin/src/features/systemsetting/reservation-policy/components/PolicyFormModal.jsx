import './PolicyFormModal.css';

export default function PolicyFormModal({ editTarget, form, onFormChange, onSubmit, onClose, submitting }) {
    const set = (key, value) => onFormChange(f => ({ ...f, [key]: value }));

    return (
        <div className="pfm__overlay" onClick={onClose}>
            <div className="pfm__box" onClick={e => e.stopPropagation()}>
                <div className="pfm__header">
                    <span>{editTarget ? '정책 수정' : '정책 생성'}</span>
                    <button className="pfm__close" onClick={onClose}>✕</button>
                </div>

                <div className="pfm__body">
                    <label className="pfm__label">
                        정책명 *
                        <input
                            type="text"
                            className="pfm__input"
                            value={form.eventName}
                            onChange={e => set('eventName', e.target.value)}
                            placeholder="정책명을 입력하세요"
                        />
                    </label>
                    <label className="pfm__label">
                        시작일시 *
                        <input
                            type="datetime-local"
                            className="pfm__input"
                            value={form.startDate}
                            onChange={e => set('startDate', e.target.value)}
                            disabled={!!editTarget}
                        />
                    </label>
                    <label className="pfm__label">
                        세대 일일 한도 (비워두면 무제한)
                        <input
                            type="number"
                            min="1"
                            className="pfm__input"
                            value={form.dailyLimitPerHousehold}
                            onChange={e => set('dailyLimitPerHousehold', e.target.value)}
                            placeholder="횟수 입력"
                        />
                    </label>
                    <label className="pfm__label">
                        세대 월간 한도 (비워두면 무제한)
                        <input
                            type="number"
                            min="1"
                            className="pfm__input"
                            value={form.monthlyLimitPerHousehold}
                            onChange={e => set('monthlyLimitPerHousehold', e.target.value)}
                            placeholder="횟수 입력"
                        />
                    </label>
                    <label className="pfm__label">
                        동시 활성 예약 수 *
                        <input
                            type="number"
                            min="1"
                            className="pfm__input"
                            value={form.maxActiveReservations}
                            onChange={e => set('maxActiveReservations', e.target.value)}
                        />
                    </label>
                    <label className="pfm__label">
                        허용 주차 시간 (분) *
                        <input
                            type="number"
                            min="1"
                            className="pfm__input"
                            value={form.permittedMinutes}
                            onChange={e => set('permittedMinutes', e.target.value)}
                        />
                    </label>
                    <label className="pfm__checkbox-row">
                        <input
                            type="checkbox"
                            checked={form.noShowPenaltyEnabled}
                            onChange={e => set('noShowPenaltyEnabled', e.target.checked)}
                        />
                        노쇼 페널티 적용
                    </label>
                </div>

                <div className="pfm__footer">
                    <button className="pfm__btn-cancel" onClick={onClose}>취소</button>
                    <button className="pfm__btn-submit" onClick={onSubmit} disabled={submitting}>
                        {submitting ? '저장 중...' : '저장'}
                    </button>
                </div>
            </div>
        </div>
    );
}