import { useState, useMemo } from 'react';
import RejectModal from '../../components/RejectModal';
import './ApprovalRequestPage.css';

// ─── 더미 데이터 ─────────────────────────────────────────────────────
const INITIAL_DATA = [
  {
    id: 1,
    type: 'resident',
    typeLabel: '입주민 등록',
    applicant: '홍길동',
    content: '101동 1205호 입주민 등록 신청',
    status: 'pending',
    requestedAt: '2026/02/03',
    processedAt: null,
  },
  {
    id: 2,
    type: 'vehicle',
    typeLabel: '차량 등록',
    applicant: '김길동',
    content: '101동 1205호 차량 등록 신청',
    status: 'approved',
    requestedAt: '2026/02/03',
    processedAt: '2026/02/03',
  },
  {
    id: 3,
    type: 'reservation',
    typeLabel: '예약 방문',
    applicant: '이길동',
    content: '101동 1205호 방문 예약 신청',
    status: 'pending',
    requestedAt: '2026/02/03',
    processedAt: null,
  },
  {
    id: 4,
    type: 'reservation',
    typeLabel: '예약 방문',
    applicant: '김길동',
    content: '101동 1205호 방문 예약 신청',
    status: 'rejected',
    requestedAt: '2026/02/03',
    processedAt: '2026/02/05',
    rejectReason: '서류 미비',
  },
];

// ─── 상수 ────────────────────────────────────────────────────────────
const TYPE_OPTIONS = [
  { value: '',            label: '등록 유형 전체' },
  { value: 'resident',    label: '입주민 등록'    },
  { value: 'vehicle',     label: '차량 등록'      },
  { value: 'reservation', label: '예약 방문'      },
];

const STATUS_OPTIONS = [
  { value: '',         label: '상태 전체' },
  { value: 'pending',  label: '대기중'    },
  { value: 'approved', label: '승인'      },
  { value: 'rejected', label: '거절'      },
];

const STATUS_LABEL = {
  pending:  '대기중',
  approved: '승인',
  rejected: '거절',
};

// ─── 페이지 컴포넌트 ─────────────────────────────────────────────────
export default function ApprovalRequestPage() {
  const [rows, setRows] = useState(INITIAL_DATA);

  // 필터 상태
  const [searchText,   setSearchText]   = useState('');
  const [filterType,   setFilterType]   = useState('');
  const [filterStatus, setFilterStatus] = useState('');

  // 거절 모달 상태
  const [rejectTarget, setRejectTarget] = useState(null);
  const [rejectReason, setRejectReason] = useState('');

  // ── 통계 ──
  const stats = useMemo(() => ({
    total:         rows.length,
    pendingCount:  rows.filter(r => r.status === 'pending').length,
    approvedCount: rows.filter(r => r.status === 'approved').length,
    rejectedCount: rows.filter(r => r.status === 'rejected').length,
  }), [rows]);

  // ── 필터링 ──
  const filteredRows = useMemo(() => {
    const kw = searchText.trim().toLowerCase();
    return rows.filter(r => {
      if (filterType   && r.type   !== filterType)   return false;
      if (filterStatus && r.status !== filterStatus) return false;
      if (kw && !(
        r.applicant.includes(kw) ||
        r.typeLabel.includes(kw) ||
        r.content.includes(kw)
      )) return false;
      return true;
    });
  }, [rows, searchText, filterType, filterStatus]);

  // ── 승인 처리 ──
  const handleApprove = (id) => {
    setRows(prev => prev.map(r =>
      r.id === id ? { ...r, status: 'approved', processedAt: today() } : r
    ));
  };

  // ── 거절 모달 ──
  const openReject  = (row) => { setRejectTarget(row); setRejectReason(''); };
  const closeReject = ()    => { setRejectTarget(null); setRejectReason(''); };

  const confirmReject = () => {
    if (!rejectReason.trim()) return;
    setRows(prev => prev.map(r =>
      r.id === rejectTarget.id
        ? { ...r, status: 'rejected', processedAt: today(), rejectReason: rejectReason.trim() }
        : r
    ));
    closeReject();
  };

  const resetFilters = () => {
    setSearchText('');
    setFilterType('');
    setFilterStatus('');
  };

  return (
    <div className="arp__content">

      {/* 통계 카드 */}
      <div className="arp__stats">
        <StatCard colorClass="blue"  label="전체 신청" value={stats.total}         />
        <StatCard colorClass="amber" label="대기중"    value={stats.pendingCount}  />
        <StatCard colorClass="green" label="승인 완료" value={stats.approvedCount} />
        <StatCard colorClass="red"   label="거절"      value={stats.rejectedCount} />
      </div>

      {/* 검색 / 필터 바 */}
      <div className="arp__filter">
        <div className="arp__search-wrap">
          <SearchIcon />
          <input
            className="arp__search"
            type="text"
            placeholder="신청자 이름, 유형, 내용 검색..."
            value={searchText}
            onChange={e => setSearchText(e.target.value)}
          />
        </div>

        <div className="arp__divider" />

        <select
          className="arp__select"
          value={filterType}
          onChange={e => setFilterType(e.target.value)}
        >
          {TYPE_OPTIONS.map(o => (
            <option key={o.value} value={o.value}>{o.label}</option>
          ))}
        </select>

        <select
          className="arp__select"
          value={filterStatus}
          onChange={e => setFilterStatus(e.target.value)}
        >
          {STATUS_OPTIONS.map(o => (
            <option key={o.value} value={o.value}>{o.label}</option>
          ))}
        </select>

        <div className="arp__divider" />

        <button className="arp__reset-btn" onClick={resetFilters}>초기화</button>
      </div>

      {/* 테이블 */}
      <div className="arp__table-wrap">
        <table className="arp__table">
          <thead>
            <tr>
              <th>유형</th>
              <th>신청자</th>
              <th>내용</th>
              <th>상태</th>
              <th>신청일시</th>
              <th>처리일시</th>
              <th>승인/거절</th>
            </tr>
          </thead>
          <tbody>
            {filteredRows.length === 0 ? (
              <tr>
                <td colSpan={7} className="arp__empty">검색 결과가 없습니다.</td>
              </tr>
            ) : filteredRows.map(row => (
              <tr key={row.id}>
                <td><span className={`arp__type-badge arp__type-badge--${row.type}`}>{row.typeLabel}</span></td>
                <td className="arp__td--bold">{row.applicant}</td>
                <td className="arp__td--muted">{row.content}</td>
                <td><span className={`arp__status-badge arp__status-badge--${row.status}`}>{STATUS_LABEL[row.status]}</span></td>
                <td className="arp__td--mono">{row.requestedAt}</td>
                <td className="arp__td--mono">{row.processedAt ?? '–'}</td>
                <td>
                  {row.status === 'pending' ? (
                    <div className="arp__actions">
                      <button className="arp__btn-approve" onClick={() => handleApprove(row.id)}>승인</button>
                      <button className="arp__btn-reject"  onClick={() => openReject(row)}>거절</button>
                    </div>
                  ) : (
                    <span className="arp__td--done">처리 완료</span>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* 거절 사유 모달 */}
      {rejectTarget && (
        <RejectModal
          title="거절 사유 입력"
          onClose={closeReject}
          onConfirm={confirmReject}
          confirmLabel="거절 확정"
          confirmVariant="danger"
          confirmDisabled={!rejectReason.trim()}
          metaContent={
            <>
              <span className={`arp__type-badge arp__type-badge--${rejectTarget.type}`} style={{ fontSize: 11 }}>
                {rejectTarget.typeLabel}
              </span>
              <strong>{rejectTarget.applicant}</strong>
              <span className="rm__meta-sep">·</span>
              <span>{rejectTarget.content}</span>
            </>
          }
          reasonLabel="거절 사유"
          placeholder="관리자가 확인할 거절 사유를 작성해 주세요..."
          value={rejectReason}
          onChange={setRejectReason}
        />
      )}
    </div>
  );
}

// ─── StatCard ────────────────────────────────────────────────────────
function StatCard({ colorClass, label, value }) {
  return (
    <div className="arp__stat-card">
      <div className={`arp__stat-dot arp__stat-dot--${colorClass}`} />
      <div className="arp__stat-info">
        <span className="arp__stat-label">{label}</span>
        <span className="arp__stat-value">{value}</span>
      </div>
    </div>
  );
}

// ─── SearchIcon (inline SVG) ─────────────────────────────────────────
function SearchIcon() {
  return (
    <svg className="arp__search-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="11" cy="11" r="8" /><line x1="21" y1="21" x2="16.65" y2="16.65" />
    </svg>
  );
}

// ─── 날짜 헬퍼 ───────────────────────────────────────────────────────
function today() {
  const d = new Date();
  return `${d.getFullYear()}/${String(d.getMonth()+1).padStart(2,'0')}/${String(d.getDate()).padStart(2,'0')}`;
}
