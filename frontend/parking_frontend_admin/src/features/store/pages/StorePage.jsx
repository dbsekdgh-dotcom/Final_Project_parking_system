import { useCallback, useEffect, useState } from "react";
import { getStores } from "../api/storeApi";
import StoreDetailModal from "../components/StoreDetailModal";
import './StorePage.css'
import Pagination from "../../../shared/components/pagination/Pagination";

const STATUS_OPTIONS =[
    { value: '', label: '상태 전체' },
    { value: 'ACTIVE', label: '입주중' },
    { value: 'INACTIVE', label: '퇴거' },
];

export const STATUS_LABEL ={
    ACTIVE: '입주중',
    INACTIVE: '퇴거',
};

export default function StorePage() {
    const [rows, setRows] = useState([]);
    const [loading, setLoading] = useState(false);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [selectedStore, setSelectedStore] = useState(null);

    const [keyword, setKeyword] = useState('');
    const [filterStatus, setFilterStatus] = useState('');

    const [activeCount, setActiveCount] = useState(0);
    const [inactiveCount, setInactiveCount] = useState(0);

    const fetchStores = useCallback(async ()=>{
        setLoading(true);
        try{
            const params = { page, size: 10};
            if(keyword.trim()) params.keyword = keyword.trim();
            if(filterStatus) params.status = filterStatus;

            const data = await getStores(params);
            setRows(data.content ?? []);
            setTotalPages(data.totalPages);
            setTotalElements(data.totalElements);
        }finally{
            setLoading(false)
        }
    }, [page, keyword, filterStatus]);

    useEffect(() => {fetchStores();}, [fetchStores]);

    useEffect(() =>{
        const loadStats = async () => {
            try{
                const [activeRes, inactiveRes] = await Promise.all([
                    getStores({ page: 0, size: 1, status: 'ACTIVE'}),
                    getStores({ page: 0, size: 1, status: 'INACTIVE'}),
                ]);
                setActiveCount(activeRes.totalElements);
                setInactiveCount(inactiveRes.totalElements);
            }catch{}
        };
        loadStats();
    }, [rows]);

    const resetFilters = () =>{
        setKeyword(``);
        setFilterStatus(``);
        setPage(0);
    };
    
    const handleModalClose = () => setSelectedStore(null);
    const handleModalRefresh = () => {
        setSelectedStore(null);
        fetchStores();
    };

    return (
          <div className="asp__content">

              {/* 통계 카드 */}
              <div className="asp__stats">
                  <StatCard colorClass="blue"  label="전체 상가" value={totalElements} />
                  <StatCard colorClass="green" label="입주중"    value={activeCount}   />
                  <StatCard colorClass="gray"  label="퇴거"      value={inactiveCount} />
              </div>

              {/* 검색 / 필터 바 */}
              <div className="asp__filter">
                  <div className="asp__search-wrap">
                      <SearchIcon />
                      <input
                          className="asp__search"
                          placeholder="상가명 검색..."
                          value={keyword}
                          onChange={e => { setKeyword(e.target.value); setPage(0); }}
                      />
                  </div>
                  <div className="asp__divider" />
                  <select className="asp__select" value={filterStatus}
                      onChange={e => { setFilterStatus(e.target.value); setPage(0); }}>
                      {STATUS_OPTIONS.map(o => (
                          <option key={o.value} value={o.value}>{o.label}</option>
                      ))}
                  </select>
                  <div className="asp__divider" />
                  <span className="asp__total">총 {totalElements}개</span>
                  <button className="asp__reset-btn" onClick={resetFilters}>초기화</button>
              </div>

              {/* 테이블 */}
              <div className="asp__table-wrap">
                  {loading ? (
                      <div className="asp__empty">불러오는 중...</div>
                  ) : (
                      <table className="asp__table">
                          <thead>
                              <tr>
                                  <th>상가명</th>
                                  <th>위치</th>
                                  <th>상태</th>
                                  <th>등록일</th>
                              </tr>
                          </thead>
                          <tbody>
                              {rows.length === 0 ? (
                                  <tr><td colSpan={4} className="asp__empty">검색 결과가 없습니다.</td></tr>
                              ) : rows.map(row => (
                                  <tr key={row.storeId} className="asp__row"
                                      onClick={() => setSelectedStore(row)}>
                                      <td className="asp__td--bold">{row.name}</td>
                                      <td className="asp__td--muted">{row.location ?? '–'}</td>
                                      <td>
                                          <span className={`asp__badge asp__status--${row.status.toLowerCase()}`}>
                                              {STATUS_LABEL[row.status] ?? row.status}
                                          </span>
                                      </td>
                                      <td className="asp__td--mono">{formatDate(row.createdAt)}</td>
                                  </tr>
                              ))}
                          </tbody>
                      </table>
                  )}
              </div>

              <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />

              {selectedStore && (
                  <StoreDetailModal
                      store={selectedStore}
                      onClose={handleModalClose}
                      onRefresh={handleModalRefresh}
                  />
              )}
          </div>
      );
}

  function StatCard({ colorClass, label, value }) {
      return (
          <div className="asp__stat-card">
              <div className={`asp__stat-dot asp__stat-dot--${colorClass}`} />
              <div className="asp__stat-info">
                  <span className="asp__stat-label">{label}</span>
                  <span className="asp__stat-value">{value ?? 0}</span>
              </div>
          </div>
      );
  }
  function SearchIcon() {
      return (
          <svg className="asp__search-icon" width="14" height="14" viewBox="0 0 24 24"
              fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <circle cx="11" cy="11" r="8" /><line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
      );
  }

  function formatDate(isoStr) {
      if (!isoStr) return '–';
      const d = new Date(isoStr);
      return `${d.getFullYear()}/${String(d.getMonth()+1).padStart(2,'0')}/${String(d.getDate()).padStart(2,'0')}`;
  }