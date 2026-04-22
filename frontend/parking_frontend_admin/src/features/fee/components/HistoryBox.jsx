import React, { useState } from 'react'
import './historyBox.css'
import 'bootstrap/dist/css/bootstrap.min.css';
import Pagination from '../../../shared/components/pagination/Pagination';

const HistoryBox = ({title,data}) => {
    const [page,setPage]=useState(0);
    const rows=5;
    const totalPages=data?Math.ceil(data?.length/rows):0
    const totalRow=data?data.length:0
    const endData=Math.min((page+1)*rows,totalRow)
    const firstData=page*rows
    const slicedData=data?data.slice(firstData,endData):[];

    const onPageChange=(num)=>{
        setPage(num)
    }
    
  return (
    <div className='section'>
        <div className='sectionHeader'>
            <span className='title'>{title} </span>
        </div>
        <div> 
            <table className="table table-hover text-center custom-history-table">
                <thead>
                    <tr>
                        <th>버전</th><th>회차시간(분)</th><th>기본요금(원)</th><th>추가단위시간(분)</th><th>추가단위요금(원)</th>
                        <th>일 최대요금(원)</th><th>적용시작일</th><th>적용종료일</th>
                    </tr>
                </thead>
                <tbody >
                    {
                        slicedData.map(d=>{
                            return(
                            <tr key={d.version}>
                                <td>{d.version}</td>
                                <td>{d.graceMinutes}</td>
                                <td>{d.baseFee.toLocaleString()}</td>
                                <td>{d.unitMinutes}</td>
                                <td>{d.unitFee.toLocaleString()}</td>
                                <td>{d.daliyMaxFee.toLocaleString()}</td>
                                <td className="date-cell">{d.effectiveFrom.split(' ')[0]}<br/>
                                    <small className="text-muted">{d.effectiveFrom.split(' ')[1]}</small>
                                </td>
                                <td className="date-cell">{d.effectiveTo.split(' ')[0]}<br/>
                                    <small className="text-muted">{d.effectiveTo.split(' ')[1]}</small>
                                </td>
                            </tr>
                            )
                        })
                    }

                </tbody>
            </table>
        </div>
        <Pagination page={page} totalPages={totalPages} onPageChange={onPageChange}/>
    </div>
  )
}

export default HistoryBox;