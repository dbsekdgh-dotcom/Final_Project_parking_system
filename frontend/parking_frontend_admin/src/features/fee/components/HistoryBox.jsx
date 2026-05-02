import React, { useState, useEffect } from 'react'
import './historyBox.css'
import 'bootstrap/dist/css/bootstrap.min.css';
import Pagination from '../../../shared/components/pagination/Pagination';
import { useQuery } from '@tanstack/react-query';
import { searchPolicyHistoryFiltered } from '../api/feeApi';

const HistoryBox = ({ title, parkingType }) => {
    const [page, setPage] = useState(0);
    const [searchVersion, setSearchVersion] = useState("");
    const [debouncedVersion, setDebouncedVersion] = useState("");
    const [startDate, setStartDate] = useState("");
    const [endDate, setEndDate] = useState("");
    const rows = 5;

    //사용자가 타이핑을 마칠 때까지 잠시 기다렸다가 마지막에 한 번만 검색을 실행
    useEffect(() => {
        const timer = setTimeout(() => {
            setDebouncedVersion(searchVersion);
            setPage(0);
        }, 400);
        //가장 마지막에 생성된 타이머 딱 하나만 살아남게
        return () => clearTimeout(timer);
    }, [searchVersion]);

    const { data = [], isLoading, isError } = useQuery({
        queryKey: ['policyHistory', parkingType, debouncedVersion, startDate, endDate],
        queryFn: () => searchPolicyHistoryFiltered({
            parkingType,
            version: debouncedVersion ? Number(debouncedVersion) : null,
            startDate: startDate || null,
            endDate: endDate || null,
        }),
        keepPreviousData: true,
    });

    const totalPages = Math.ceil(data.length / rows);
    const slicedData = data.slice(page * rows, (page + 1) * rows);

    const resetHandler = () => {
        setSearchVersion("");
        setDebouncedVersion("");
        setStartDate("");
        setEndDate("");
        setPage(0);
    };

    return (
        <div className='section'>
            <div className='sectionHeader'>
                <span className='title'>{title}</span>
                <div className='search-container'>
                    <input type="number" className="form-control search-input version-input" placeholder="버전 검색"
                        value={searchVersion} onChange={(e) => setSearchVersion(e.target.value)} />
                    <div className="date-group">
                        <input type="date" className="form-control search-input" value={startDate}
                            onChange={(e) => { setStartDate(e.target.value); setPage(0); }} />
                        <span>~</span>
                        <input type="date" className="form-control search-input" value={endDate}
                            onChange={(e) => { setEndDate(e.target.value); setPage(0); }} />
                    </div>
                    <button className='reset-button' onClick={resetHandler}>초기화</button>
                </div>
            </div>

            {isLoading && <div className="text-center py-3">불러오는 중...</div>}
            {isError && <div className="text-center py-3 text-danger">이력을 불러오지 못했습니다.</div>}

            <div className="table-responsive">
                <table className="table table-hover text-center custom-history-table">
                    <thead>
                        <tr>
                            <th>버전</th><th>회차시간(분)</th><th>기본요금(원)</th><th>추가단위시간(분)</th><th>추가단위요금(원)</th>
                            <th>일 최대요금(원)</th><th>적용시작일</th><th>적용종료일</th>
                        </tr>
                    </thead>
                    <tbody>
                        {slicedData.map(d => (
                            <tr key={d.version}>
                                <td>{d.version}</td>
                                <td>{d.graceMinutes}</td>
                                <td>{d.baseFee.toLocaleString()}</td>
                                <td>{d.unitMinutes}</td>
                                <td>{d.unitFee.toLocaleString()}</td>
                                <td>{d.daliyMaxFee.toLocaleString()}</td>
                                <td className="date-cell">{d.effectiveFrom.split(' ')[0]}<br />
                                    <small className="text-muted">{d.effectiveFrom.split(' ')[1]}</small>
                                </td>
                                <td className="date-cell">{d.effectiveTo.split(' ')[0]}<br />
                                    <small className="text-muted">{d.effectiveTo.split(' ')[1]}</small>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
            <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
        </div>
    );
};

export default HistoryBox;
