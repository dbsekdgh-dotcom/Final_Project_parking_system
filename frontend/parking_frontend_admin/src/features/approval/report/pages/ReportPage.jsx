import { useState, useCallback, useEffect } from 'react';
  import RejectModal from '../../components/RejectModal';
  import Pagination from '../../../../shared/components/pagination/Pagination';
  import './ReportPage.css';
  import { getReports, approveReport, rejectReport } from '../api/reportApi';
  import { usePendingCountRefresh } from '../../../../shared/context/PendingCountContext';

  const TYPE_OPTIONS = [
    { value: '',                label: '유형 전체'  },
    { value: 'DOUBLE_PARK',     label: '이중주차'   },
    { value: 'BLOCKING',        label: '통행방해'   },
    { value: 'NOISE',           label: '소음'       },
    { value: 'ILLEGAL_PARKING', label: '불법주차'   },
    { value: 'OTHER',           label: '기타'       },
  ];

  const STATUS_OPTIONS = [
    { value: '',          label: '상태 전체' },
    { value: 'PENDING',   label: '대기중'    },
    { value: 'APPROVED',  label: '승인'      },
    { value: 'REJECTED',  label: '거절'      },
    { value: 'CANCELLED', label: '취소'      },
  ];

  const TYPE_LABEL = {
    DOUBLE_PARK:     '이중주차',
    BLOCKING:        '통행방해',
    NOISE:           '소음',
    ILLEGAL_PARKING: '불법주차',
    OTHER:           '기타',
  };

  const STATUS_LABEL = {
    PENDING:   '대기중',
    APPROVED:  '승인',
    REJECTED:  '거절',
    CANCELLED: '취소',
  };

  export default function ReportPage() {
    const refreshCounts = usePendingCountRefresh()
    const [rows, setRows]               = useState([]);
    const [loading, setLoading]         = useState(false);
    const [page, setPage]               = useState(0);
    const [totalPages, setTotalPages]   = useState(0);
    const [totalElements, setTotalElements] = useState(0);

    const [searchText,   setSearchText]   = useState('');
    const [filterType,   setFilterType]   = useState('');
    const [filterStatus, setFilterStatus] = useState('');

    const [stats, setStats] = useState({
      pendingCount: 0, approvedCount: 0, rejectedCount: 0,
    });

    // 상세 모달
    const [detailTarget, setDetailTarget] = useState(null);

    // 거절 모달
    const [rejectTarget, setRejectTarget] = useState(null);
    const [rejectReason, setRejectReason] = useState('');

    // ── 목록 조회 ──────────────────────────────────────────────
    const fetchReports = useCallback(async () => {
      setLoading(true);
      try {
        const params = { page, size: 10 };
        if (filterType)        params.type    = filterType;
        if (filterStatus)      params.status  = filterStatus;
        if (searchText.trim()) params.keyword = searchText.trim();

        const data = await getReports(params);
        setRows(data.content ?? []);
        setTotalPages(data.totalPages);
        setTotalElements(data.totalElements);
        setStats(data.stats ?? { pendingCount: 0, approvedCount: 0, rejectedCount: 0 });
      } finally {
        setLoading(false);
      }
    }, [page, filterType, filterStatus, searchText]);

    useEffect(() => { fetchReports(); }, [fetchReports]);

    // ── 필터 ──────────────────────────────────────────────────
    const handleTypeChange   = e => { setFilterType(e.target.value);   setPage(0); };
    const handleStatusChange = e => { setFilterStatus(e.target.value); setPage(0); };
    const handleSearchChange = e => { setSearchText(e.target.value);   setPage(0); };
    const resetFilters = () => { setSearchText(''); setFilterType(''); setFilterStatus(''); };

    // ── 승인 ──────────────────────────────────────────────────
    const handleApprove = async (reportId) => {
      await approveReport(reportId);
      setDetailTarget(null);
      fetchReports();
      refreshCounts();
    };

    // ── 거절 ──────────────────────────────────────────────────
    const openReject  = (row) => { setRejectTarget(row); setRejectReason(''); };
    const closeReject = ()    => { setRejectTarget(null); setRejectReason(''); };

    const confirmReject = async () => {
      if (!rejectReason.trim()) return;
      await rejectReport(rejectTarget.reportId, rejectReason.trim());
      closeReject();
      setDetailTarget(null);
      fetchReports();
      refreshCounts();
    };

    return (
      <div className="arp__content">

        {/* 통계 카드 */}
        <div className="arp__stats">
          <StatCard colorClass="blue"  label="전체 신고" value={totalElements}        />
          <StatCard colorClass="amber" label="대기중"    value={stats.pendingCount}   />
          <StatCard colorClass="green" label="승인 완료" value={stats.approvedCount}  />
          <StatCard colorClass="red"   label="거절"      value={stats.rejectedCount}  />
        </div>

        {/* 필터 바 */}
        <div className="arp__filter">
          <div className="arp__search-wrap">
            <SearchIcon />
            <input
              className="arp__search"
              type="text"
              placeholder="차량번호, 신고자 이름 검색..."
              value={searchText}
              onChange={handleSearchChange}
            />
          </div>
          <div className="arp__divider" />
          <select className="arp__select" value={filterType}   onChange={handleTypeChange}>
            {TYPE_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
          </select>
          <select className="arp__select" value={filterStatus} onChange={handleStatusChange}>
            {STATUS_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
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
                  <th>신고 유형</th>
                  <th>신고자</th>
                  <th>차량번호</th>
                  <th>상태</th>
                  <th>신고일시</th>
                  <th>처리일시</th>
                  <th>상세</th>
                </tr>
              </thead>
              <tbody>
                {rows.length === 0 ? (
                  <tr><td colSpan={7} className="arp__empty">검색 결과가 없습니다.</td></tr>
                ) : rows.map(row => (
                  <tr key={row.reportId}>
                    <td>
                      <span className={`arp__type-badge rp__type-badge--${row.reportType?.toLowerCase()}`}>
                        {TYPE_LABEL[row.reportType] ?? row.reportType}
                      </span>
                    </td>
                    <td className="arp__td--bold">{row.reporterName}</td>
                    <td className="arp__td--mono">{row.carNumber}</td>
                    <td>
                      <span className={`arp__status-badge arp__status-badge--${row.status?.toLowerCase()}`}>
                        {STATUS_LABEL[row.status] ?? row.status}
                      </span>
                    </td>
                    <td className="arp__td--mono">{formatDate(row.createdAt)}</td>
                    <td className="arp__td--mono">{row.resolvedAt ? formatDate(row.resolvedAt) : '–'}</td>
                    <td>
                      <button className="rp__detail-btn" onClick={() => setDetailTarget(row)}>
                        상세보기
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />

        {/* 상세 모달 */}
        {detailTarget && (
          <div className="rm__overlay" onClick={() => setDetailTarget(null)}>
            <div className="rp__detail-modal" onClick={e => e.stopPropagation()}>

              <div className="rm__header">
                <h3>신고 상세</h3>
                <button className="rm__close" onClick={() => setDetailTarget(null)}>✕</button>
              </div>

              <div className="rp__detail-body">
                <div className="rp__detail-row">
                  <span className="rp__detail-label">신고 유형</span>
                  <span className={`arp__type-badge rp__type-badge--${detailTarget.reportType?.toLowerCase()}`}>
                    {TYPE_LABEL[detailTarget.reportType] ?? detailTarget.reportType}
                  </span>
                </div>
                <div className="rp__detail-row">
                  <span className="rp__detail-label">신고자</span>
                  <span>{detailTarget.reporterName}</span>
                </div>
                <div className="rp__detail-row">
                  <span className="rp__detail-label">차량번호</span>
                  <span className="arp__td--mono">{detailTarget.carNumber}</span>
                </div>
                <div className="rp__detail-row">
                  <span className="rp__detail-label">상세 내용</span>
                  <span className="rp__detail-desc">{detailTarget.description || '내용 없음'}</span>
                </div>
                {detailTarget.imageUrl && (
                  <div className="rp__detail-row rp__detail-row--col">
                    <span className="rp__detail-label">첨부 사진</span>
                    <img
                      className="rp__detail-img"
                      src={detailTarget.imageUrl}
                      alt="신고 사진"
                    />
                  </div>
                )}
                <div className="rp__detail-row">
                  <span className="rp__detail-label">신고일시</span>
                  <span className="arp__td--mono">{formatDate(detailTarget.createdAt)}</span>
                </div>
                {detailTarget.resolvedAt && (
                  <div className="rp__detail-row">
                    <span className="rp__detail-label">처리일시</span>
                    <span className="arp__td--mono">{formatDate(detailTarget.resolvedAt)}</span>
                  </div>
                )}
              </div>

              {detailTarget.status === 'PENDING' && (
                <div className="rm__footer">
                  <button
                    className="arp__btn-reject"
                    onClick={() => openReject(detailTarget)}
                  >
                    거절
                  </button>
                  <button
                    className="arp__btn-approve"
                    onClick={() => handleApprove(detailTarget.reportId)}
                  >
                    승인
                  </button>
                </div>
              )}

            </div>
          </div>
        )}

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
                <span className={`arp__type-badge rp__type-badge--${rejectTarget.reportType?.toLowerCase()}`}
                  style={{ fontSize: 11 }}>
                  {TYPE_LABEL[rejectTarget.reportType]}
                </span>
                <strong>{rejectTarget.reporterName}</strong>
                <span className="rm__meta-sep">·</span>
                <span>{rejectTarget.carNumber}</span>
              </>
            }
            reasonLabel="거절 사유"
            placeholder="거절 사유를 입력해 주세요..."
            value={rejectReason}
            onChange={setRejectReason}
          />
        )}
      </div>
    );
  }

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

  function SearchIcon() {
    return (
      <svg className="arp__search-icon" width="14" height="14" viewBox="0 0 24 24"
        fill="none" stroke="currentColor" strokeWidth="2"
        strokeLinecap="round" strokeLinejoin="round">
        <circle cx="11" cy="11" r="8" /><line x1="21" y1="21" x2="16.65" y2="16.65" />
      </svg>
    );
  }

  function formatDate(isoStr) {
    if (!isoStr) return '–';
    const d = new Date(isoStr);
    return `${d.getFullYear()}/${String(d.getMonth()+1).padStart(2,'0')}/${String(d.getDate()).padStart(2,'0')}`;
  }
