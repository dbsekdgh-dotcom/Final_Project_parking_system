import { useState, useCallback, useEffect } from 'react';
import { fetchReservations, fetchReservationDetail } from '../api/ManagementApi';
import Pagination from '../../../shared/components/pagination/Pagination';
import { SearchIcon, formatDate, formatDateTime } from '../components/ManagementHelpers.jsx';
import ReservationDetailModal from '../components/ReservationDetailModal';
import '../../approval/approval-request/pages/ApprovalRequestPage.css';
import '../pages/management.css';

const STATUS_OPTIONS = [
  { value: '',          label: '상태 전체' },
  { value: 'RESERVED',  label: '예약'     },
  { value: 'ENTERED',   label: '입차'     },
  { value: 'COMPLETED', label: '완료'     },
  { value: 'CANCELLED', label: '취소'     },
  { value: 'REJECTED',  label: '거절'     },
  { value: 'NO_SHOW',   label: '노쇼'     },
];
const STATUS_LABEL = {
  RESERVED:  '예약',
  ENTERED:   '입차',
  COMPLETED: '완료',
  CANCELLED: '취소',
  REJECTED:  '거절',
  NO_SHOW:   '노쇼',
};

export default function UserVehicleReservation() {
  const [rows, setRows]             = useState([]);
  const [loading, setLoading]       = useState(false);
  const [page, setPage]             = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [keyword, setKeyword]       = useState('');
  const [status, setStatus]         = useState('');
  const [detail, setDetail]         = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const data = await fetchReservations({ keyword, status, page, size: 10 });
      setRows(data.content ?? []);
      setTotalPages(data.totalPages ?? 0);
    } finally {
      setLoading(false);
    }
  }, [keyword, status, page]);

  useEffect(() => { load(); }, [load]);

  const handleRowClick = async (reservationId) => {
    setDetailLoading(true);
    try {
      const data = await fetchReservationDetail(reservationId);
      setDetail(data);
    } finally {
      setDetailLoading(false);
    }
  };

  const reset = () => { setKeyword(''); setStatus(''); setPage(0); };

  return (
    <div className="arp__content">
      <div className="arp__filter">
        <div className="arp__search-wrap">
          <SearchIcon />
          <input
            className="arp__search"
            placeholder="차량번호, 이름 검색..."
            value={keyword}
            onChange={(e) => { setKeyword(e.target.value); setPage(0); }}
          />
        </div>
        <div className="arp__divider" />
        <select className="arp__select" value={status}
          onChange={(e) => { setStatus(e.target.value); setPage(0); }}>
          {STATUS_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
        </select>
        <div className="arp__divider" />
        <button className="arp__reset-btn" onClick={reset}>초기화</button>
      </div>

      <div className="arp__table-wrap">
        {loading ? (
          <div className="arp__empty">불러오는 중...</div>
        ) : (
          <table className="arp__table">
            <thead>
              <tr>
                <th>차량번호</th><th>신청자</th><th>방문 시작</th>
                <th>방문 종료</th><th>신청일</th><th>상태</th>
              </tr>
            </thead>
            <tbody>
              {rows.length === 0 ? (
                <tr><td colSpan={6} className="arp__empty">검색 결과가 없습니다.</td></tr>
              ) : rows.map(row => (
                <tr key={row.reservationId} className="clickable" onClick={() => handleRowClick(row.reservationId)}>
                  <td className="arp__td--bold arp__td--mono">{row.carNumber}</td>
                  <td className="arp__td--muted">{row.userName}</td>
                  <td className="arp__td--mono">{formatDateTime(row.visitStartAt)}</td>
                  <td className="arp__td--mono">{formatDateTime(row.visitEndAt)}</td>
                  <td className="arp__td--mono">{formatDate(row.createdAt)}</td>
                  <td>
                    <span className={`arp__status-badge arp__status-badge--${row.status.toLowerCase()}`}>
                      {STATUS_LABEL[row.status] ?? row.status}
                    </span>
                  </td>
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
      {detail && <ReservationDetailModal data={detail} onClose={() => setDetail(null)} />}
    </div>
  );
}
