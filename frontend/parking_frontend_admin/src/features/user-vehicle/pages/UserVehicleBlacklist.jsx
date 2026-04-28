import { useState, useCallback, useEffect } from 'react';
import { fetchBlacklist, fetchBlacklistDetail, registerBlacklist } from '../api/ManagementApi';
import Pagination from '../../../shared/components/pagination/Pagination';
import { SearchIcon, formatDateTime } from '../components/ManagementHelpers.jsx';
import BlacklistDetailModal from '../components/BlacklistDetailModal';
import '../../approval/approval-request/pages/ApprovalRequestPage.css';
import '../pages/management.css';

const STATUS_OPTIONS = [
    { value: '',         label: '상태 전체' },
    { value: 'ACTIVE',   label: '차단중'   },
    { value: 'RELEASED', label: '해제'     },
];
const REASON_OPTIONS = [
    { value: '',                   label: '사유 전체'      },
    { value: 'REPORT_ACCUMULATION',label: '신고 누적'      },
    { value: 'ILLEGAL_VEHICLE',    label: '불법 차량'      },
    { value: 'USER_BLACKLIST',     label: '사용자 차단'    },
    { value: 'ADMIN_MANUAL',       label: '관리자 직접 등록'},
    { value: 'SYSTEM_BLOCK',       label: '시스템 차단'    },
];
// 관리자가 직접 등록 가능한 사유만
const REGISTER_REASON_OPTIONS = [
    { value: 'ILLEGAL_VEHICLE', label: '불법 차량'       },
    { value: 'USER_BLACKLIST',  label: '사용자 차단'     },
    { value: 'ADMIN_MANUAL',    label: '관리자 직접 등록' },
];
const REASON_LABEL = {
    REPORT_ACCUMULATION: '신고 누적',
    ILLEGAL_VEHICLE:     '불법 차량',
    USER_BLACKLIST:      '사용자 차단',
    ADMIN_MANUAL:        '관리자 직접 등록',
    SYSTEM_BLOCK:        '시스템 차단',
};

const INIT_FORM = { carNumber: '', reasonType: 'ADMIN_MANUAL', reasonDetail: '', permanent: true, endDate: '' };

export default function UserVehicleBlacklist() {
    const [rows, setRows]           = useState([]);
    const [loading, setLoading]     = useState(false);
    const [page, setPage]           = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [keyword, setKeyword]     = useState('');
    const [status, setStatus]       = useState('');
    const [reasonType, setReasonType] = useState('');

    const [detail, setDetail]             = useState(null);
    const [detailLoading, setDetailLoading] = useState(false);

    const [showForm, setShowForm] = useState(false);
    const [form, setForm]         = useState(INIT_FORM);
    const [submitting, setSubmitting] = useState(false);

    const load = useCallback(async () => {
        setLoading(true);
        try {
            const data = await fetchBlacklist({ keyword, status, reasonType, page, size: 10 });
            setRows(data.content ?? []);
            setTotalPages(data.totalPages ?? 0);
        } finally {
            setLoading(false);
        }
    }, [keyword, status, reasonType, page]);

    useEffect(() => { load(); }, [load]);

    const handleRowClick = async (id) => {
        setDetailLoading(true);
        try {
            const data = await fetchBlacklistDetail(id);
            setDetail(data);
        } finally {
            setDetailLoading(false);
        }
    };

    const reset = () => { setKeyword(''); setStatus(''); setReasonType(''); setPage(0); };

    const handleFormChange = (e) => {
        const { name, value, type, checked } = e.target;
        setForm(prev => ({ ...prev, [name]: type === 'checkbox' ? checked : value }));
    };

    const handleRegister = async (e) => {
        e.preventDefault();
        if (!form.carNumber.trim()) { alert('차량번호를 입력해주세요.'); return; }
        if (!form.reasonType)       { alert('차단 사유를 선택해주세요.'); return; }
        if (!form.permanent && !form.endDate) { alert('차단 종료일을 입력하거나 영구 차단을 선택해주세요.'); return; }

        if (!window.confirm(`[${form.carNumber}] 차량을 블랙리스트에 등록하시겠습니까?`)) return;

        setSubmitting(true);
        try {
            await registerBlacklist({
                carNumber:    form.carNumber.trim(),
                reasonType:   form.reasonType,
                reasonDetail: form.reasonDetail.trim() || null,
                endDate:      form.permanent ? null : form.endDate,
            });
            alert('블랙리스트에 등록되었습니다.');
            setForm(INIT_FORM);
            setShowForm(false);
            load();
        } catch (err) {
            const msg = err?.response?.data?.message;
            alert(msg ?? '등록 중 오류가 발생했습니다.');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="arp__content">
            {/* 필터 + 등록 버튼 */}
            <div className="arp__filter">
                <div className="arp__search-wrap">
                    <SearchIcon />
                    <input
                        className="arp__search"
                        placeholder="차량번호 검색..."
                        value={keyword}
                        onChange={(e) => { setKeyword(e.target.value); setPage(0); }}
                    />
                </div>
                <div className="arp__divider" />
                <select className="arp__select" value={status}
                    onChange={(e) => { setStatus(e.target.value); setPage(0); }}>
                    {STATUS_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
                </select>
                <select className="arp__select" value={reasonType}
                    onChange={(e) => { setReasonType(e.target.value); setPage(0); }}>
                    {REASON_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
                </select>
                <div className="arp__divider" />
                <button className="arp__reset-btn" onClick={reset}>초기화</button>
                <button
                    className="arp__reset-btn"
                    style={{ marginLeft: 'auto', background: '#7f1d1d', color: '#fca5a5', border: '1px solid #991b1b' }}
                    onClick={() => setShowForm(v => !v)}
                >
                    {showForm ? '닫기' : '+ 등록'}
                </button>
            </div>

            {/* 등록 폼 */}
            {showForm && (
                <form onSubmit={handleRegister}
                    style={{
                        background: '#1e1e1e', border: '1px solid #2e2e2e', borderRadius: 10,
                        padding: '20px 24px', marginBottom: 16,
                        display: 'flex', flexDirection: 'column', gap: 12,
                    }}>
                    <span style={{ fontWeight: 600, color: '#f9fafb', fontSize: 14 }}>블랙리스트 등록</span>
                    <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
                        <input
                            name="carNumber" value={form.carNumber} onChange={handleFormChange}
                            placeholder="차량번호 (예: 12가3456)" required
                            className="arp__search"
                            style={{ flex: '1 1 140px', minWidth: 140 }}
                        />
                        <select name="reasonType" value={form.reasonType} onChange={handleFormChange}
                            className="arp__select" style={{ flex: '1 1 140px' }}>
                            {REGISTER_REASON_OPTIONS.map(o =>
                                <option key={o.value} value={o.value}>{o.label}</option>)}
                        </select>
                        <label style={{ display: 'flex', alignItems: 'center', gap: 6, color: '#d1d5db', fontSize: 13, cursor: 'pointer' }}>
                            <input type="checkbox" name="permanent" checked={form.permanent} onChange={handleFormChange} />
                            영구 차단
                        </label>
                        {!form.permanent && (
                            <input type="datetime-local" name="endDate" value={form.endDate} onChange={handleFormChange}
                                className="arp__search" style={{ flex: '1 1 180px' }} />
                        )}
                    </div>
                    <textarea
                        name="reasonDetail" value={form.reasonDetail} onChange={handleFormChange}
                        placeholder="상세 사유 (선택)"
                        rows={2}
                        style={{
                            background: '#141414', border: '1px solid #2e2e2e', borderRadius: 6,
                            color: '#f9fafb', fontSize: 13, padding: '8px 10px', resize: 'vertical',
                        }}
                    />
                    <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
                        <button type="button" className="arp__reset-btn" onClick={() => setShowForm(false)}>취소</button>
                        <button type="submit" className="arp__reset-btn"
                            style={{ background: '#7f1d1d', color: '#fca5a5', border: '1px solid #991b1b' }}
                            disabled={submitting}>
                            {submitting ? '등록 중...' : '등록'}
                        </button>
                    </div>
                </form>
            )}

            {/* 목록 테이블 */}
            <div className="arp__table-wrap">
                {loading ? (
                    <div className="arp__empty">불러오는 중...</div>
                ) : (
                    <table className="arp__table">
                        <thead>
                            <tr>
                                <th>차량번호</th><th>차단 사유</th><th>차단 시작</th>
                                <th>차단 종료</th><th>상태</th><th>등록일</th>
                            </tr>
                        </thead>
                        <tbody>
                            {rows.length === 0 ? (
                                <tr><td colSpan={6} className="arp__empty">검색 결과가 없습니다.</td></tr>
                            ) : rows.map(row => (
                                <tr key={row.id} className="clickable" onClick={() => handleRowClick(row.id)}>
                                    <td className="arp__td--bold arp__td--mono">{row.carNumber}</td>
                                    <td>{REASON_LABEL[row.reasonType] ?? row.reasonType}</td>
                                    <td className="arp__td--mono">{formatDateTime(row.startDate)}</td>
                                    <td className="arp__td--mono">
                                        {row.permanent ? <span style={{ color: '#f87171' }}>영구</span> : formatDateTime(row.endDate)}
                                    </td>
                                    <td>
                                        <span className={`arp__status-badge arp__status-badge--${row.status.toLowerCase()}`}>
                                            {row.status === 'ACTIVE' ? '차단중' : '해제'}
                                        </span>
                                    </td>
                                    <td className="arp__td--mono">{formatDateTime(row.createdAt)}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>

            <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />

            {detailLoading && (
                <div className="mgmt-modal-overlay">
                    <div className="arp__empty" style={{ color: '#fff' }}>불러오는 중...</div>
                </div>
            )}
            {detail && (
                <BlacklistDetailModal
                    data={detail}
                    onClose={() => setDetail(null)}
                    onRefresh={load}
                />
            )}
        </div>
    );
}
