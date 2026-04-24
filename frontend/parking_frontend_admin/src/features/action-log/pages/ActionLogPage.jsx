import React, { useCallback, useEffect, useState } from 'react'
import { getActionLogs, revertActionLog } from '../api/actionLogApi';
import Pagination from '../../../shared/components/pagination/Pagination';
import ActionLogDetailModal from '../components/ActionLogDetailModal';
import './ActionLogPage.css';

const TARGET_OPTIONS =[
    { value : '', label : '대상 유형 전체' },
    { value : 'USER', label : '사용자' },
    { value : 'VEHICLE', label : '차량' },
    { value : 'PAYMENT', label : '결제' },
    { value : 'POLICY', label : '정책' },
    { value : 'RESERVATION', label : '예약' },
    { value : 'SYSTEM_SETTING', label : '시스템 설정' },
    { value : 'STORE', label : '상가' },
    { value : 'PARKING_LOG', label : '주차 로그' },
    { value : 'REPORT', label : '신고' },
];

const ACTION_OPTIONS = [
    { value : '', label : '작업 유형 전체' },
    { value : 'CREATE', label : '생성' },
    { value : 'UPDATE', label : '수정' },
    { value : 'DELETE', label : '삭제' },
    { value : 'APPROVE', label : '승인' },
    { value : 'REJECT', label : '거절' },
    { value : 'REFUND', label : '환불' },
    { value : 'REPORT', label : '신고' },
    { value : 'BLACKLIST', label : '블랙리스트'},
    { value : 'ACTIVE', label : '활성화' },
    { value : 'INACTIVE', label : '비활성화' },
]
const REVERTED_OPTIONS = [
    { value : '', label : '되돌림 전체' },
    { value : 'false', label : '되돌림 가능' },
    { value : 'true', label : '되돌림 완료' },
]

export const TARGET_LABEL = {
    USER: '사용자', VEHICLE: '차량', PAYMENT: '결제', POLICY: '정책',
    RESERVATION: '예약', SYSTEM_SETTING: '시스템 설정', STORE: '상점',
    PARKING_LOG: '주차 로그', REPORT: '신고',
}
export const ACTION_LABEL ={
    CREATE: '생성', UPDATE: '수정', DELETE: '삭제', APPROVE: '승인', REJECT: '거절',
    REFUND: '환불', REPORT: '신고', BLACKLIST: '블랙리스트', ACTIVE: '활성화',INACTIVE: '비활성화',
}

export default function ActionLogPage() {
     const [rows, setRows] = useState([]);
    const [loading, setLoading] = useState(false);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [selectedLog, setSelectedLog] = useState(null);

    const [keyword, setKeyword] = useState('');
    const [filterTarget, setFilterTarget] = useState('');
    const [filterAction, setFilterAction] = useState('');
    const [filterReverted, setFilterReverted] = useState('');
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');

    const fetchLogs = useCallback(async () =>{
        setLoading(true);
        try{
            const params = { page, size: 20 };
            if(filterTarget) params.targetType = filterTarget;
            if(filterAction) params.actionType = filterAction;
            if(filterReverted !== '') params.isReverted = filterReverted;
            if(keyword.trim()) params.keyword = keyword.trim();
            if(startDate) params.startDate = new Date(startDate).toISOString();
            if(endDate) params.endDate = new Date(endDate).toISOString();

            const data = await getActionLogs(params);
            setRows(data.content ?? []);
            setTotalPages(data.totalPages);
            setTotalElements(data.totalElements);
        } finally{
            setLoading(false);
        }
    }, [page, filterTarget, filterAction, filterReverted, keyword, startDate, endDate]);
    useEffect(() => { fetchLogs(); }, [fetchLogs]);

    const resetFilters = () => {
        setKeyword(''); setFilterAction(''); setFilterTarget('');
        setFilterReverted(''); setStartDate(''); setEndDate('');
        setPage(0);
    };
    
    const handleRevert = async (actionId) => {
        await revertActionLog(actionId);
        setSelectedLog(null);
        fetchLogs();
    };
     return (
      <div className="aal__content">
        <div className="aal__filter">
          <div className="aal__search-wrap">
            <SearchIcon />
            <input
              className="aal__search"
              placeholder="관리자 이름 검색..."
              value={keyword}
              onChange={e => { setKeyword(e.target.value); setPage(0); }}
            />
          </div>
          <div className="aal__divider" />
          <select className="aal__select" value={filterTarget}
            onChange={e => { setFilterTarget(e.target.value); setPage(0); }}>
            {TARGET_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
          </select>
          <select className="aal__select" value={filterAction}
            onChange={e => { setFilterAction(e.target.value); setPage(0); }}>
            {ACTION_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
          </select>
          <select className="aal__select" value={filterReverted}
            onChange={e => { setFilterReverted(e.target.value); setPage(0); }}>
            {REVERTED_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
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
                  <th>관리자</th>
                  <th>대상 유형</th>
                  <th>작업 유형</th>
                  <th>대상 ID</th>
                  <th>일시</th>
                  <th>되돌림</th>
                </tr>
              </thead>
              <tbody>
                {rows.length === 0 ? (
                  <tr><td colSpan={6} className="aal__empty">검색 결과가 없습니다.</td></tr>
                ) : rows.map(row => (
                  <tr key={row.actionId} className="aal__row" onClick={() => setSelectedLog(row)}>
                    <td className="aal__td--bold">{row.adminName}</td>
                    <td>
                      <span className={`aal__badge aal__target--${row.targetType.toLowerCase().replace('_', '-')}`}>
                        {TARGET_LABEL[row.targetType] ?? row.targetType}
                      </span>
                    </td>
                    <td>
                      <span className={`aal__badge aal__action--${row.actionType.toLowerCase()}`}>
                        {ACTION_LABEL[row.actionType] ?? row.actionType}
                      </span>
                    </td>
                    <td className="aal__td--mono">{row.targetId ?? '–'}</td>
                    <td className="aal__td--mono">{formatDateTime(row.createdAt)}</td>
                    <td>
                      {row.isReverted
                        ? <span className="aal__badge aal__reverted--done">완료</span>
                        : row.actionType === 'UPDATE'
                          ? <span className="aal__badge aal__reverted--possible">가능</span>
                          : <span className="aal__reverted--na">–</span>}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />

        {selectedLog && (
          <ActionLogDetailModal
            log={selectedLog}
            onClose={() => setSelectedLog(null)}
            onRevert={handleRevert}
          />
        )}
      </div>
    );
}
 function SearchIcon() {
    return (
      <svg className="aal__search-icon" width="14" height="14" viewBox="0 0 24 24"
        fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <circle cx="11" cy="11" r="8" /><line x1="21" y1="21" x2="16.65" y2="16.65" />
      </svg>
    );
  }

  function formatDateTime(isoStr) {
    if (!isoStr) return '–';
    const d = new Date(isoStr);
    return `${d.getFullYear()}/${String(d.getMonth()+1).padStart(2,'0')}/${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`;
  }