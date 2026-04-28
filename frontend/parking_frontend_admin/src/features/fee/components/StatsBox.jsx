import React, { useEffect, useState } from 'react'
import {getStats} from './../api/feeApi'
import { useQuery } from '@tanstack/react-query'
import './statsBox.css'
import 'bootstrap/dist/css/bootstrap.min.css';
import Pagination from '../../../shared/components/pagination/Pagination';
import  RevenueChart  from './RevenueChart';

const StatsBox = ({title}) => {
    const [isOpen,setIsOpen]=useState(false);

    //한달 자료를 기본으로 조회
    const getMonthAgo=()=>{
        const now=new Date();
        now.setMonth(now.getMonth()-1)
        return now.toISOString().split('T')[0]
    }
    const [start,setStart]=useState(getMonthAgo)
    const [end,setEnd]=useState(new Date().toISOString().split('T')[0])

    //데이터 요청
    const {data,isError,isLoading}=useQuery({
        queryKey:['stats',start,end],
        queryFn:async()=> await getStats({start,end}),
        enabled: !!start && !! end
    })

    //페이징 처리
    const [page,setPage]=useState(0);
    const dailyList = data?.dailyStats || [];
    const rows=5;
    const totalPages=data?Math.ceil(dailyList.length/rows):0
    const totalRow=dailyList?dailyList.length:0
    const endData=Math.min((page+1)*rows,totalRow)
    const firstData=page*rows
    const slicedData=dailyList?dailyList.slice(firstData,endData):[];
    const onPageChange=(num)=>{
        setPage(num)
    }

    //기간 조회 설정이 변경되면 첫 페이지로 
    useEffect(()=>{
        setPage(0)
    },[start,end])

    const endDateHandler=(e)=>{
        if(start>e){
            alert("종료일은 시작일보다 빠를 수 없습니다.")
            setEnd(start)
        }else{
            setEnd(e)
        }
    }

    const startDateHandler=(e)=>{
        if(e>end){
            alert("종료일은 시작일보다 빠를 수 없습니다.")
            setStart(end)
        }else{
            setStart(e)
        }
    }

  return (
    <div className='section'>
        <div className='sectionHeader'>
            <span className='title'>{title} </span>
            {/* 날짜 조회 */}
            <div className='date'>
                <input type='date' value={start} onChange={e=>startDateHandler(e.target.value)} placeholder='시작일'/>
                <input type='date' value={end} onChange={e=>endDateHandler(e.target.value)} placeholder='종료일'/>
            </div>
        </div>
        { data &&
        <div>
            {/* 요약 카드 */}
            <div className='summaryGrid'>
                <div className='summaryCard'>
                    <span className='cardLabel'>총 수익</span>
                    <span className='cardValue'>{data.revenue.toLocaleString()}원</span>
                </div>
                <div className='summaryCard'>
                    <span className='cardLabel'>총 환불액</span>
                    <span className='cardValue'>{data.refund.toLocaleString()}원</span>
                </div>
                <div className='summaryCard'>
                    <span className='cardLabel'>총 비용</span>
                    <span className='cardValue'>{data.cost.toLocaleString()}원</span>
                </div>
                <div className='summaryCard'>
                    <span className='cardLabel'>순이익</span>
                    <span className='cardValue'>{data.net.toLocaleString()}원</span>
                </div>
            </div>
            <div className='chartContainer'>
                {
                    dailyList.length>0 ?
                    (<RevenueChart dailyList={dailyList}></RevenueChart>):
                    <div>조회된 데이터가 없습니다.</div>
                }
                
            </div>
            {/* 상세 내용 */}
            {
            <div className="upcomingSection">
                <span className="msg">본 통계는 결제 완료 시점을 기준으로 집계되었으며, 무료 출차 및 할인권 사용 내역은 운영 로그를 참조하시기 바랍니다.</span>
                <div onClick={()=>setIsOpen(!isOpen)} className="upcomingToggle" >
                <span>상세 내역 {isOpen?'닫기 ▲':'보기 ▼' }</span>
                </div>
                {isOpen && slicedData && slicedData.length>0 &&
                <div >
                    <table className="table table-hover text-center custom-history-table">
                        <thead>
                            <tr>
                                <th>날짜</th><th>주차장 수익</th><th>정기권 수익</th><th>할인권 수익</th><th>환불액</th>
                                <th>카드수수료</th><th>포인트할인</th><th>순이익</th>
                            </tr>
                        </thead>
                        <tbody >
                            {
                                slicedData.map(d=>{
                                    return(
                                    <tr key={d.date}>
                                        <td>{d.date}</td>
                                        <td>{d.parkingRevenue.toLocaleString()}</td>
                                        <td>{d.subscriptionRevenue.toLocaleString()}</td>
                                        <td>{d.ticketRevenue.toLocaleString()}</td>
                                        <td>{d.refund.toLocaleString()}</td>
                                        <td>{d.commission.toLocaleString()}</td>
                                        <td>{d.pointDiscount.toLocaleString()}</td>
                                        <td>{d.net.toLocaleString()}</td>
                                    </tr>
                                    )
                                })
                            }

                        </tbody>
                    </table>
                    <Pagination page={page} totalPages={totalPages} onPageChange={onPageChange}/>
                    {!isOpen && isLoading && <div>데이터를 불러오는 중입니다...</div>}
                    
                </div>
                }
            </div>
            }

        </div>
        }
        
    </div>
  )
}

export default StatsBox;