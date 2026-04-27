import React, { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query'; 
import { getAnalysis } from './../api/feeApi';
import Pagination from '../../../shared/components/pagination/Pagination';
import './revenueAnalysisBox.css';
import RevenuePieChart from './RevenuePieChart';

const RevenueAnalysisBox = ({ title }) => {
    const [isDetail, setIsDetail] = useState(false);

    // 한달 자료를 기본으로 조회
    const getMonthAgo = () => {
        const now = new Date();
        now.setMonth(now.getMonth() - 1);
        return now.toISOString().split('T')[0];
    };

    const [start, setStart] = useState(getMonthAgo());
    const [end, setEnd] = useState(new Date().toISOString().split('T')[0]);

    // 데이터 요청
    const { data, isLoading } = useQuery({
        queryKey: ['analysis', start, end],
        queryFn: async () => await getAnalysis({ start, end }),
        enabled: !!start && !!end
    });

    const endDateHandler = (e) => {
        if (start > e) {
            alert("종료일은 시작일보다 빠를 수 없습니다.");
            setEnd(start);
        } else {
            setEnd(e);
        }
    };

    const startDateHandler = (e) => {
        if (e > end) {
            alert("시작일은 종료일보다 늦을 수 없습니다.");
            setStart(end);
        } else {
            setStart(e);
        }
    };

    return (
        <div className='section'>
            <div className='sectionHeader'>
                <span className='title'>{title}</span>
                <div className='date'>
                    <input type='date' value={start} onChange={e => startDateHandler(e.target.value)} />
                    <input type='date' value={end} onChange={e => endDateHandler(e.target.value)} />
                </div>
            </div>

            {data && (
                <div className='analysisContent'>
                    {/* [좌측] 그래프 영역 */}
                    <div className='chartSide'>
                        <div className='chartContainer'>
                            {
                                data.amount>0?
                                (<RevenuePieChart data={data}/>) :
                                (<div>조회된 데이터가 없습니다.</div>)
                            }
                            
                        </div>
                    </div>

                    {/* [우측] 요약 정보 / 상세 내역 */}
                    <div className='infoSide'>
                        <div className='infoBoxContent'>
                            {!isDetail ? (
                                <div className='verticalCardList animate-fade-in'>
                                    <div className='summaryCard sideCard highlight'>
                                        <span className='cardLabel'>총 매출액</span>
                                        <span className='cardValue'>{data.amount.toLocaleString()}원</span>
                                    </div>
                                    <div className='summaryCard sideCard'>
                                        <span className='cardLabel'>총 매출건수</span>
                                        <span className='cardValue'>{data.exitCount.toLocaleString()}건</span>
                                    </div>
                                    <div className='summaryCard sideCard'>
                                        <span className='cardLabel'>평균 객단가</span>
                                        <span className='cardValue'>
                                            {data.exitCount > 0 ? Math.floor(data.amount / data.exitCount).toLocaleString() : 0}원
                                        </span>
                                    </div>
                                    <div className='buttonArea' style={{ marginTop: 'auto' }}>
                                        <button className='toggleBtn' onClick={() => setIsDetail(true)}>상세보기</button>
                                    </div>
                                </div>
                            ) : (
                                <div className='tableWrapper animate-fade-in'>
                                    <div className='infoBoxHeader mini-header'>
                                        <span className='subTitle'>일자별 상세 내역 (최신순)</span>
                                    </div>
                                    <div className='scrollArea'>
                                        <table className="table text-center custom-history-table mini-table">
                                            <thead>
                                                <tr>
                                                    <th>날짜</th>
                                                    <th>카드</th>
                                                    <th>포인트</th>
                                                    <th>합계</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                {data.dailyRevenue.length > 0 ? (
                                                    data.dailyRevenue.map((d) => (
                                                        <tr key={d.date}>
                                                            <td>{d.date.slice(5)}</td>
                                                            <td>{d.payAmount.toLocaleString()}</td>
                                                            <td>{d.pointAmount.toLocaleString()}</td>
                                                            <td style={{ fontWeight: 'bold', color: '#2563eb' }}>{d.totalAmount.toLocaleString()}</td>
                                                        </tr>
                                                    ))
                                                ) : (
                                                    <tr><td colSpan="4">데이터가 없습니다.</td></tr>
                                                )}
                                            </tbody>
                                        </table>
                                    </div>
                                    <div className='buttonArea' style={{ marginTop: '15px' }}>
                                        <button className='toggleBtn' onClick={() => setIsDetail(false)}>요약보기</button>
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default RevenueAnalysisBox;