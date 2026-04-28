import { useEffect, useState } from 'react';
import './StoreDetailModal.css';
import {
    activateStore, deactivateStore, getStore, updateStore,
    getStoreTicketConfig, setStoreTicketConfig, getFreeTicketPolicies
} from '../api/storeApi';

export default function StoreDetailModal({ store, onClose, onRefresh }) {
    const [detail, setDetail]                     = useState(null);
    const [loading, setLoading]                   = useState(true);

    // 수정 (이름, 비밀번호 선택 — 미입력 시 기존값 유지)
    const [editName, setEditName]                 = useState('');
    const [editPassword, setEditPassword]         = useState('');
    const [saving, setSaving]                     = useState(false);

    // 입주 (이름, 비밀번호 둘 다 필수)
    const [activateName, setActivateName]         = useState('');
    const [activatePassword, setActivatePassword] = useState('');
    const [actioning, setActioning]               = useState(false);

    // 할인권 config (미선택/미입력 시 기존값 유지)
    const [freePolicies, setFreePolicies]         = useState([]);
    const [selectedPolicyId, setSelectedPolicyId] = useState('');
    const [quota, setQuota]                       = useState('');
    const [configSaving, setConfigSaving]         = useState(false);

    useEffect(() => {
        const load = async () => {
            try {
                const [d, policies, config] = await Promise.all([
                    getStore(store.storeId),
                    getFreeTicketPolicies(),
                    getStoreTicketConfig(store.storeId),
                ]);
                setDetail(d);
                setEditName(d.name);
                setActivateName(d.name);
                setFreePolicies(policies);
                if (config) {
                    setSelectedPolicyId(String(config.ticketPolicyId));
                    setQuota(String(config.monthlyQuota));
                }
            } finally {
                setLoading(false);
            }
        };
        load();
    }, []);

    const isInactive = detail?.status === 'INACTIVE';

    // 수정 — 입력된 값만 반영, 미입력은 기존값 유지
    const handleUpdate = async () => {
        setSaving(true);
        try {
            const dto = {};
            if (editName.trim() && editName.trim() !== detail.name) dto.name = editName.trim();
            if (editPassword.trim()) dto.terminalPassword = editPassword.trim();
            if (Object.keys(dto).length > 0) {
                await updateStore(store.storeId, dto);
                onRefresh();
            }
        } finally {
            setSaving(false);
        }
    };

    // 입주 — 이름 + 비밀번호 둘 다 필수
    const handleActivate = async () => {
        if (!activateName.trim()) {
            alert('상가명을 입력해주세요.');
            return;
        }
        if (!activatePassword.trim()) {
            alert('단말기 비밀번호를 입력해주세요.');
            return;
        }
        if (!window.confirm(`[${activateName.trim()}] 상가를 입주 처리하시겠습니까?`)) return;
        setActioning(true);
        try {
            await activateStore(store.storeId, {
                name: activateName.trim(),
                terminalPassword: activatePassword.trim(),
            });
            onRefresh();
        } finally {
            setActioning(false);
        }
    };

    // 퇴거 처리
    const handleDeactivate = async () => {
        if (!window.confirm(`[${detail.name}] 상가를 퇴거 처리합니다.\n연결된 모든 지갑과 할인권 설정이 초기화됩니다. 계속하시겠습니까?`)) return;
        setActioning(true);
        try {
            await deactivateStore(store.storeId);
            onRefresh();
        } finally {
            setActioning(false);
        }
    };

    // 할인권 config 저장 — 수량 필수(1이상), 종류 미선택 시 기존 유지
    const handleConfigSave = async () => {
        const q = parseInt(quota, 10);
        if (!quota || isNaN(q) || q < 1) {
            alert('월 지급 수량을 1 이상으로 입력해주세요.');
            return;
        }
        setConfigSaving(true);
        try {
            await setStoreTicketConfig(store.storeId, {
                ticketPolicyId: selectedPolicyId ? Number(selectedPolicyId) : null,
                monthlyQuota: q,
            });
            alert('할인권 설정이 저장되었습니다.');
        } finally {
            setConfigSaving(false);
        }
    };

    return (
        <div className="sdm__overlay" onClick={onClose}>
            <div className="sdm__modal" onClick={e => e.stopPropagation()}>

                <div className="sdm__header">
                    <h2 className="sdm__title">상가 상세</h2>
                    <button className="sdm__close" onClick={onClose}>✕</button>
                </div>

                {loading ? (
                    <div className="sdm__loading">불러오는 중...</div>
                ) : (
                    <div className="sdm__body">

                        {/* 기본 정보 */}
                        <section className="sdm__section">
                            <h3 className="sdm__section-title">기본 정보</h3>
                            <div className="sdm__info-grid">
                                <InfoRow label="상가 ID"    value={detail.storeId} />
                                <InfoRow label="위치"       value={detail.location ?? '–'} />
                                <InfoRow label="상태"       value={
                                    <span className={`sdm__badge sdm__status--${detail.status.toLowerCase()}`}>
                                        {detail.status === 'ACTIVE' ? '입주중' : '퇴거'}
                                    </span>
                                } />
                                <InfoRow label="등록일"     value={formatDate(detail.createdAt)} />
                                <InfoRow label="최종수정일"  value={formatDate(detail.updatedAt)} />
                            </div>
                        </section>

                        {/* 정보 수정 — ACTIVE 상태에서만, 이름/비밀번호 선택사항 */}
                        {!isInactive && (
                            <section className="sdm__section">
                                <h3 className="sdm__section-title">정보 수정</h3>
                                <div className="sdm__form">
                                    <label className="sdm__label">상가명 (미입력 시 기존 유지)</label>
                                    <input className="sdm__input" value={editName}
                                        onChange={e => setEditName(e.target.value)} />
                                    <label className="sdm__label">단말기 비밀번호 (미입력 시 기존 유지)</label>
                                    <input className="sdm__input" type="password"
                                        value={editPassword}
                                        placeholder="변경하지 않으면 비워두세요"
                                        onChange={e => setEditPassword(e.target.value)} />
                                    <button className="sdm__btn sdm__btn--primary"
                                        onClick={handleUpdate} disabled={saving}>
                                        {saving ? '저장 중...' : '저장'}
                                    </button>
                                </div>
                            </section>
                        )}

                        {/* 월 무료 할인권 설정 */}
                        <section className="sdm__section">
                            <h3 className="sdm__section-title">월 무료 할인권 설정</h3>
                            <div className="sdm__form">
                                <label className="sdm__label">할인권 종류 (미선택 시 기존 유지)</label>
                                <select className="sdm__input"
                                    value={selectedPolicyId}
                                    disabled={isInactive}
                                    onChange={e => setSelectedPolicyId(e.target.value)}>
                                    <option value="">-- 선택 --</option>
                                    {freePolicies.map(p => (
                                        <option key={p.ticketPolicyId} value={p.ticketPolicyId}>
                                            {p.name}
                                        </option>
                                    ))}
                                </select>
                                <label className="sdm__label">월 지급 수량</label>
                                <input className="sdm__input" type="number" min="1"
                                    value={quota}
                                    disabled={isInactive}
                                    placeholder="수량을 입력하세요"
                                    onChange={e => setQuota(e.target.value)} />
                                {isInactive
                                    ? <p className="sdm__action-desc">퇴거 상태에서는 할인권 설정을 변경할 수 없습니다.</p>
                                    : (
                                        <button className="sdm__btn sdm__btn--primary"
                                            onClick={handleConfigSave} disabled={configSaving}>
                                            {configSaving ? '저장 중...' : '할인권 설정 저장'}
                                        </button>
                                    )
                                }
                            </div>
                        </section>

                        {/* 입주 / 퇴거 처리 */}
                        <section className="sdm__section">
                            <h3 className="sdm__section-title">상태 변경</h3>
                            {isInactive ? (
                                <div className="sdm__form">
                                    <label className="sdm__label">상가명 *</label>
                                    <input className="sdm__input"
                                        value={activateName}
                                        onChange={e => setActivateName(e.target.value)}
                                        placeholder="상가명을 입력하세요" />
                                    <label className="sdm__label">단말기 비밀번호 *</label>
                                    <input className="sdm__input" type="password"
                                        value={activatePassword}
                                        onChange={e => setActivatePassword(e.target.value)}
                                        placeholder="비밀번호를 입력하세요" />
                                    <button className="sdm__btn sdm__btn--activate"
                                        onClick={handleActivate} disabled={actioning}>
                                        {actioning ? '처리 중...' : '입주 처리'}
                                    </button>
                                </div>
                            ) : (
                                <div className="sdm__action-row">
                                    <button className="sdm__btn sdm__btn--deactivate"
                                        onClick={handleDeactivate} disabled={actioning}>
                                        {actioning ? '처리 중...' : '퇴거 처리'}
                                    </button>
                                    <p className="sdm__action-desc">
                                        퇴거 처리 시 연결된 모든 지갑과 할인권 설정이 초기화됩니다.
                                    </p>
                                </div>
                            )}
                        </section>

                    </div>
                )}
            </div>
        </div>
    );
}

function InfoRow({ label, value }) {
    return (
        <>
            <span className="sdm__info-label">{label}</span>
            <span className="sdm__info-value">{value}</span>
        </>
    );
}

function formatDate(isoStr) {
    if (!isoStr) return '–';
    const d = new Date(isoStr);
    return `${d.getFullYear()}/${String(d.getMonth() + 1).padStart(2, '0')}/${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
}
