import { useState, useMemo, useCallback, useEffect } from 'react';
import RejectModal from '../../components/RejectModal';
import './ApprovalRequestPage.css';
import Pagination from '../../../../shared/components/pagination/Pagination';

  
// ─── 상수 ────────────────────────────────────────────────────────────
const TYPE_OPTIONS = [
  { value: '',            label: '등록 유형 전체' },
  { value: 'RESIDENT',    label: '입주민 등록'    },
  { value: 'VEHICLE',     label: '차량 등록'      },
  { value: 'RESERVATION', label: '예약 방문'      },
];

const STATUS_OPTIONS = [
  { value: '',         label: '상태 전체' },
  { value: 'PENDING',  label: '대기중'    },
  { value: 'APPROVAL', label: '승인'      },
  { value: 'REJECTED', label: '거절'      },
];

const STATUS_LABEL = {
  PENDING:  '대기중',
  APPROVAL: '승인',
  REJECTED: '거절',
  CANCELLED: '취소',
};

const TYPE_LABEL = {
  RESIDENT: '입주민 등록',
  VEHICLE: '차량 등록',
  RESERVATION: '방문 예약',
}

// 페이지 컴포넌트 
export default function ApprovalRequestPage() {
  const [rows, setRows] = useState([]);
  const [loading,setLoading] = useState(false);
  const [page,setPage] = useState(0);
  const [totalPages,setTotalPages] = useState(0);
  const [totalElements,setTotalElements] = useState(0);

  // 필터 상태
  const [searchText,   setSearchText]   = useState('');
  const [filterType,   setFilterType]   = useState('');
  const [filterStatus, setFilterStatus] = useState('');

  // 거절 모달 상태
  const [rejectTarget, setRejectTarget] = useState(null);
  const [rejectReason, setRejectReason] = useState('');

  // ── 통계 ──
  const [stats,setStats] = useState({
    total: 0, pendingCount: 0, approvedCount: 0, rejectedCount: 0,
  });

  // API 조회
  const fetchApprovals = useCallback(async ()=>{
    setLoading(true);
    try{
      const params = new URLSearchParams({ page, size: 10});
      if (filterType) params.append('type', filterType);
      if (filterStatus) params.append('status', filterStatus);
      if (searchText.trim) params.append('keyword', searchText.trim());

      const res = await fetch(`/api/admin/approvals?${params}`, { 
        credentials:'include',
        headers: { 'Authorization': `Bearer ${localStorage.getItem('accessToken')}`}
      });
      const data = await res.json();
      setRows(data.content ?? []);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
      setStats(data.stats ?? {
        total: data.totalElements,
        pendingCount: 0,
        approvedCount: 0,
        rejectedCount: 0,
      });
    }finally{
      setLoading(false);
    }
  }, [page, filterType, filterStatus, searchText]);

  useEffect(()=>{
    fetchApprovals();
  },[fetchApprovals]);

  // 필터 변경 ( 초기화 )
  const handleTypeChange = (e) =>{
    setFilterType(e.target.value);
    setPage(0)
  }
  const handleStatusChange = (e) =>{
    setFilterStatus(e.target.value);
    setPage(0)
  }
  const handleSearchChange = (e) =>{
    setSearchText(e.target.value);
    setPage(0)
  }

   const resetFilters = () => {
    setSearchText('');
    setFilterType('');
    setFilterStatus('');
  };

  //  승인 
  const handleApprove = async(approvalId) => {
    await fetch(`/api/admin/approvals/${approvalId}/approve`,{
       method : 'POST',
       credentials:'include',
       headers : { 'Authorization': `Bearer ${localStorage.getItem('accessToken')}`}
      });
    fetchApprovals();
  };

 // 거절 모달
  const openReject = (row) => { setRejectTarget(row); setRejectReason(''); };
  const closeReject = () => { setRejectTarget(null); setRejectReason(''); };

  const confirmReject = async () => {
    if (!rejectReason.trim()) return;
    await fetch(`/api/admin/approvals/${rejectTarget.approvalId}/reject`,{
      method : 'POST',
      headers : { 'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
      },
      body : JSON.stringify({ rejectReason: rejectReason.trim() }),
      credentials : 'include',
    });
    closeReject();
    fetchApprovals();
  };

 

 

  return (
    <div className="arp__content">

      {/* 통계 카드 */}
      <div className="arp__stats">
        <StatCard colorClass="blue"  label="전체 신청" value={totalElements}         />
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
            onChange={handleSearchChange}
          />
        </div>

        <div className="arp__divider" />

        <select
          className="arp__select"
          value={filterType}
          onChange={handleTypeChange}
        >
          {TYPE_OPTIONS.map(o => (
            <option key={o.value} value={o.value}>{o.label}</option>
          ))}
        </select>

        <select
          className="arp__select"
          value={filterStatus}
          onChange={handleStatusChange}
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
          {loading ? (
            <div className="arp__empty">불러오는 중...</div>
          ) : (
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
                {rows.length === 0 ? (
                  <tr>
                    <td colSpan={7} className="arp__empty">검색 결과가 없습니다.</td>
                  </tr>
                ) : rows.map(row => (
                  <tr key={row.approvalId}>
                    <td>
                      <span className={`arp__type-badge arp__type-badge--${row.approvalType.toLowerCase()}`}>
                        {TYPE_LABEL[row.approvalType] ?? row.approvalType}
                      </span>
                    </td>
                    <td className="arp__td--bold">{row.requestUserName}</td>
                    <td className="arp__td--muted">
                      {row.content}
                      {row.approvalType === 'RESERVATION' && row.visitStartAt && (
                        <div style={{ fontSize: 11, color: '#888', marginTop: 2 }}>
                          방문: {formatDateTime(row.visitStartAt)} ~ {formatDateTime(row.visitEndAt)}
                        </div>
                      )}
                    </td>
                    <td>
                      <span className={`arp__status-badge arp__status-badge--${row.status.toLowerCase()}`}>
                        {STATUS_LABEL[row.status] ?? row.status}
                      </span>
                    </td>
                    <td className="arp__td--mono">{formatDate(row.createdAt)}</td>
                    <td className="arp__td--mono">{row.processedAt ? formatDate(row.processedAt) : '–'}</td>
                    <td>
                      {row.status === 'PENDING' ? (
                        <div className="arp__actions">
                          <button className="arp__btn-approve" onClick={() => handleApprove(row.approvalId)}>승인</button>
                          <button className="arp__btn-reject"  onClick={() => openReject(row)}>거절</button>
                        </div>
                      ) : (
                        <span className="arp__td--done">
                          {row.processedBySystem ? '시스템 처리' : '관리자 처리'}
                        </span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

      <Pagination
        page={page}
        totalPages={totalPages}
        onPageChange={setPage}
      />

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
              <span className={`arp__type-badge arp__type-badge--${rejectTarget.approvalType.toLowerCase()}`} style={{ fontSize: 11 }}>
                {TYPE_LABEL[rejectTarget.approvalType]}
              </span>
              <strong>{rejectTarget.requestUserName}</strong>
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
        <span className="arp__stat-value">{value ?? 0}</span>
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
function formatDate(isoStr) {
  if (!isoStr) return '-';
  const d = new Date(isoStr);
  return `${d.getFullYear()}/${String(d.getMonth()+1).padStart(2,'0')}/${String(d.getDate()).padStart(2,'0')}`;
}
function formatDateTime(isoStr){
  if (!isoStr) return '-';
  const d = new Date(isoStr);
  return `${formatDate(isoStr)} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`;}
