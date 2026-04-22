import { useState, useCallback, useEffect } from 'react';
import { fetchVehicles, fetchVehicleDetail } from '../api/ManagementApi';
import Pagination from '../../../shared/components/pagination/Pagination';
import { SearchIcon, formatDate } from '../components/ManagementHelpers.jsx';
import VehicleDetailModal from '../components/VehicleDetailModal';
import '../../approval/approval-request/pages/ApprovalRequestPage.css';
import '../pages/management.css';

const STATUS_OPTIONS = [
  { value: '',        label: '상태 전체' },
  { value: 'ACTIVE',  label: '활성'     },
  { value: 'DELETED', label: '삭제'     },
];
const STATUS_LABEL = { ACTIVE: '활성', PENDING: '대기', DELETED: '삭제' };

export default function UserVehicleVehicle() {
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
      const data = await fetchVehicles({ keyword, status, page, size: 10 });
      setRows(data.content ?? []);
      setTotalPages(data.totalPages ?? 0);
    } finally {
      setLoading(false);
    }
  }, [keyword, status, page]);

  useEffect(() => { load(); }, [load]);

  const handleRowClick = async (vehicleId) => {
    setDetailLoading(true);
    try {
      const data = await fetchVehicleDetail(vehicleId);
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
                <th>차량번호</th><th>차량명</th><th>소유자</th>
                <th>등록일</th><th>상태</th>
              </tr>
            </thead>
            <tbody>
              {rows.length === 0 ? (
                <tr><td colSpan={5} className="arp__empty">검색 결과가 없습니다.</td></tr>
              ) : rows.map(row => (
                <tr key={row.vehicleId} className="clickable" onClick={() => handleRowClick(row.vehicleId)}>
                  <td className="arp__td--bold arp__td--mono">{row.carNumber}</td>
                  <td>{row.vehicleName}</td>
                  <td className="arp__td--muted">{row.ownerName ?? '-'}</td>
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
      {detail && <VehicleDetailModal data={detail} onClose={() => setDetail(null)} />}
    </div>
  );
}
