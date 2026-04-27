import { useEffect, useState } from 'react';
import './StoreDetailModal.css';
import { activateStore, deactivateStore, getStore, updateStore } from '../api/storeApi';

export default function StoreDetailModal({ store, onClose, onRefresh}){

    const [detail, setDetail] = useState(null);
    const [loadingDetail, setLoadingDetail] = useState(true);

    const [editName, setEditName] = useState('');
    const [editPassword, setEditPassword] = useState('');
    const [saving, setSaving] = useState(false);

    const [actioning, setActioning] = useState(false);

    useEffect(()=>{
        const load = async () => {
            try{
                const d =await getStore(store.storeId);
                setDetail(d);
                setEditName(d.name);
            } finally {
                setLoadingDetail(false);
            }
        };
        load();
    },[]);

    const handleUpdate = async () => {
        if (!editName.trim()) return;
        setSaving(true);
        try{
            const dto = {};
            if(editName.trim() !== detail.name) dto.name = editName.trim();
            if(editPassword.trim()) dto.terminalPassword = editPassword.trim();
            if(Object.keys(dto).length === 0) return;

            await updateStore(store.storeId, dto);
            onRefresh();
        }finally {
            setSaving(false);
        }
    };

    const handleActivate = async () => {
        if (!window.confirm(`[${detail.name}] 상가를 입주 처리하시겠습니까?`)) return;
        setActioning(true);
        try{
            const dto ={};
            if(editName.trim() && editName.trim() !== detail) dto.name = editName.trim();
            if(editPassword.trim()) dto.terminalPassword = editPassword.trim();
            if(Object.keys(dto).length > 0){
                await updateStore(store.storeId, dto);
            }
            await activateStore(store.storeId);
            onRefresh();
        }finally{
            setActioning(false);
        }
    };

    const handleDeactivate = async () => {
        if (!window.confirm(`[${detail.name}] 상가를 퇴거 처리합니다. \n연결된 모든 지갑이 초기화됩니다. 계속하시겠습니까?`)) return;
        setActioning(true);
        try{
            await deactivateStore(store.storeId);
            onRefresh();
        }finally{
            setActioning(false);
        }
    };
     return (
          <div className="sdm__overlay" onClick={onClose}>
              <div className="sdm__modal" onClick={e => e.stopPropagation()}>

                  {/* 헤더 */}
                  <div className="sdm__header">
                      <h2 className="sdm__title">상가 상세</h2>
                      <button className="sdm__close" onClick={onClose}>✕</button>
                  </div>

                  {loadingDetail ? (
                      <div className="sdm__loading">불러오는 중...</div>
                  ) : (
                      <div className="sdm__body">

                          {/* 기본 정보 */}
                          <section className="sdm__section">
                              <h3 className="sdm__section-title">기본 정보</h3>
                              <div className="sdm__info-grid">
                                  <InfoRow label="상가 ID"   value={detail.storeId} />
                                  <InfoRow label="위치"      value={detail.location ?? '–'} />
                                  <InfoRow label="상태"      value={
                                      <span className={`sdm__badge sdm__status--${detail.status.toLowerCase()}`}>
                                          {detail.status === 'ACTIVE' ? '입주중' : '퇴거'}
                                      </span>
                                  } />
                                  <InfoRow label="등록일"    value={formatDate(detail.createdAt)} />
                                  <InfoRow label="최종수정일" value={formatDate(detail.updatedAt)} />
                              </div>
                          </section>

                          {/* 수정 폼 */}
                          <section className="sdm__section">
                              <h3 className="sdm__section-title">정보 수정</h3>
                              <div className="sdm__form">
                                  <label className="sdm__label">상가명</label>
                                  <input className="sdm__input" value={editName}
                                      onChange={e => setEditName(e.target.value)} />
                                  <label className="sdm__label">단말기 비밀번호 (변경 시 입력)</label>
                                  <input className="sdm__input" type="password"
                                      placeholder="변경하지 않으면 비워두세요"
                                      value={editPassword}
                                      onChange={e => setEditPassword(e.target.value)} />
                                  <button className="sdm__btn sdm__btn--primary"
                                      onClick={handleUpdate} disabled={saving || !editName.trim()}>
                                      {saving ? '저장 중...' : '저장'}
                                  </button>
                              </div>
                          </section>

                          {/* 입주 / 퇴거 처리 */}
                          <section className="sdm__section">
                              <h3 className="sdm__section-title">상태 변경</h3>
                              <div className="sdm__action-row">
                                  {detail.status === 'INACTIVE' ? (
                                      <button className="sdm__btn sdm__btn--activate"
                                          onClick={handleActivate} disabled={actioning}>
                                          {actioning ? '처리 중...' : '입주 처리'}
                                      </button>
                                  ) : (
                                      <button className="sdm__btn sdm__btn--deactivate"
                                          onClick={handleDeactivate} disabled={actioning}>
                                          {actioning ? '처리 중...' : '퇴거 처리'}
                                      </button>
                                  )}
                                  <p className="sdm__action-desc">
                                      {detail.status === 'ACTIVE'
                                          ? '퇴거 처리 시 연결된 모든 지갑이 초기화됩니다.'
                                          : '입주 처리 시 상태가 활성화됩니다.'}
                                  </p>
                              </div>
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
      return `${d.getFullYear()}/${String(d.getMonth()+1).padStart(2,'0')}/${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`;
  }