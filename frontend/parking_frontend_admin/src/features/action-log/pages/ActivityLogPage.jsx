import { useState, useCallback, useEffect } from 'react';
  import { getActivityLogs, getActivityLog } from '../api/actionLogApi';
  import Pagination from '../../../shared/components/pagination/Pagination';
  import ActivityLogDetailModal from '../components/ActivityLogDetailModal';
  import './ActionLogPage.css';

  const ACTIVITY_TYPE_OPTIONS = [
      { value: '',                       label: '활동유형 전체' },
      { value: 'ENTRY',                  label: '입차' },
      { value: 'EXIT',                   label: '출차' },
      { value: 'PAYMENT_PRE',            label: '사전정산' },
      { value: 'PAYMENT_EXIT',           label: '출차정산' },
      { value: 'PAYMENT_CANCEL',         label: '결제취소' },
      { value: 'RESERVATION_CREATED',    label: '방문예약 생성' },
      { value: 'RESERVATION_CANCELLED',  label: '방문예약 취소' },
      { value: 'RESIDENT_REGISTERED',    label: '입주민 등록' },
      { value: 'VEHICLE_REGISTERED',     label: '차량 등록' },
      { value: 'PASS_PURCHASED',         label: '정기권 구매' },
      { value: 'COUPON_PURCHASED',       label: '할인권 구매' },
      { value: 'COUPON_USED',            label: '할인 적용' },
      { value: 'ADMIN_FORCE_EXIT',       label: '관리자 강제출차' },
      { value: 'REFUNDED',               label: '환불' },
  ];

  export default function ActivityLogPage() {
      const [rows,          setRows]          = useState([]);
      const [loading,       setLoading]       = useState(false);
      const [page,          setPage]          = useState(0);
      const [totalPages,    setTotalPages]    = useState(0);
      const [totalElements, setTotalElements] = useState(0);
      const [selectedLog,   setSelectedLog]   = useState(null);
      const [keyword,       setKeyword]       = useState('');
      const [filterType,    setFilterType]    = useState('');
      const [startDate,     setStartDate]     = useState('');
      const [endDate,       setEndDate]       = useState('');

      const fetchLogs = useCallback(async () => {
          setLoading(true);
          try {
              const params = { page, size: 20 };
              if (keyword.trim()) params.keyword      = keyword.trim();
              if (filterType)     params.activityType = filterType;
              if (startDate)      params.startDate    = new Date(startDate).toISOString();
              if (endDate)        params.endDate      = new Date(endDate).toISOString();
              const data = await getActivityLogs(params);
              setRows(data.content ?? []);
              setTotalPages(data.totalPages);
              setTotalElements(data.totalElements);
          } finally {
              setLoading(false);
          }
      }, [page, keyword, filterType, startDate, endDate]);

      useEffect(() => { fetchLogs(); }, [fetchLogs]);

      const handleRowClick = async (row) => {
          const detail = await getActivityLog(row.activityId);
          setSelectedLog(detail);
      };

      const resetFilters = () => {
          setKeyword(''); setFilterType('');
          setStartDate(''); setEndDate(''); setPage(0);
      };

      return (
          <div className="aal__content">
              <div className="aal__filter">
                  <div className="aal__search-wrap">
                      <SearchIcon />
                      <input
                          className="aal__search"
                          placeholder="차량번호 또는 사용자 이름 검색..."
                          value={keyword}
                          onChange={e => { setKeyword(e.target.value); setPage(0); }}
                      />
                  </div>
                  <div className="aal__divider" />
                  <select className="aal__select" value={filterType}
                      onChange={e => { setFilterType(e.target.value); setPage(0); }}>
                      {ACTIVITY_TYPE_OPTIONS.map(o =>
                          <option key={o.value} value={o.value}>{o.label}</option>
                      )}
                  </select>
                  <div className="aal__divider" />
                  <input className="aal__date" type="date" value={startDate}
                      onChange={e => { setStartDate(e.target.value); setPage(0); }} />
                  <span className="aal__date-sep">~</span>
                  <input className="aal__date" type="date" value={endDate}
                      onChange={e => { setEndDate(e.target.value); setPage(0); }} />
                  <div className="aal__divider" />
                  <span className="aal__total">총 {totalElements}건</span>
                  <button className="aal__reset-btn" onClick={resetFilters}>초기화</button>
              </div>

              <div className="aal__table-wrap">
                  {loading ? (
                      <div className="aal__empty">불러오는 중...</div>
                  ) : (
                      <table className="aal__table">
                          <thead>
                              <tr>
                                  <th>ID</th>
                                  <th>활동유형</th>
                                  <th>사용자</th>
                                  <th>동호수</th>
                                  <th>차량번호</th>
                                  <th>메시지</th>
                                  <th>발생일시</th>
                              </tr>
                          </thead>
                          <tbody>
                              {rows.length === 0 ? (
                                  <tr>
                                      <td colSpan={7} className="aal__empty">검색 결과가 없습니다.</td>
                                  </tr>
                              ) : rows.map(row => (
                                  <tr key={row.activityId} className="aal__row"
                                      onClick={() => handleRowClick(row)}>
                                      <td className="aal__td--mono">{row.activityId}</td>
                                      <td>
                                          <span className={`aal__badge aal__activity--${row.activityType.toLowerCase().replace(/_/g, '-')}`}>
                                              {row.activityTypeLabel}
                                          </span>
                                      </td>
                                      <td className="aal__td--bold">{row.userName ?? '비회원'}</td>
                                      <td>{row.unitNo != null ? `${row.unitNo}호` : '–'}</td>
                                      <td className="aal__td--mono">{row.carNumber ?? '–'}</td>
                                      <td className="aal__td--gray">{row.message}</td>
                                      <td className="aal__td--mono">{formatDateTime(row.createdAt)}</td>
                                  </tr>
                              ))}
                          </tbody>
                      </table>
                  )}
              </div>

              <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />

              {selectedLog && (
                  <ActivityLogDetailModal
                      log={selectedLog}
                      onClose={() => setSelectedLog(null)}
                  />
              )}
          </div>
      );
  }

  function SearchIcon() {
      return (
          <svg className="aal__search-icon" width="14" height="14" viewBox="0 0 24 24"
              fill="none" stroke="currentColor" strokeWidth="2"
              strokeLinecap="round" strokeLinejoin="round">
              <circle cx="11" cy="11" r="8" />
              <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
      );
  }

  function formatDateTime(isoStr) {
      if (!isoStr) return '–';
      const d = new Date(isoStr);
      return `${d.getFullYear()}/${String(d.getMonth()+1).padStart(2,'0')}/${String(d.getDate()).padStart(2,'0')}
  ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`;
  }