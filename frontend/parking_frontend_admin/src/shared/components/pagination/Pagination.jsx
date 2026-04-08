import React from 'react'
import './Pagination.css'

const Pagination = ({ page, totalPages, onPageChange }) => {
    // 페이징 관련 계산
    const pageGroupSize = 5; //한번에 보여줄 페이지 번호 개수
    const currentGroup = Math.floor(page / pageGroupSize); //현재 몇번째 그룹인지
    const startPage = currentGroup * pageGroupSize //그룹 시작 번호
    const endPage = Math.min(startPage + pageGroupSize, totalPages) //그룹의 끝 번호
    //  페이징 배열 생성 [startPage, ..., endPage-1]
    const pageNumbers = []
    for (let i = startPage; i < endPage; i++) {
        pageNumbers.push(i);
    }
    
    if(totalPages===0) return null;

    return (
        <div className='pagination-container'>
            {/* 페이징 UI */}
            {/* 맨 처음으로(<<) */}
            <button className='nav-btn' onClick={() => onPageChange(0)} disabled={page === 0}>
                {"<<"}
            </button>
            {/* 이전 그룹으로(<) */}
            <button className='nav-btn' onClick={() => onPageChange(Math.max(0, startPage - 1))} disabled={startPage === 0}>
                {"<"}
            </button>
            {/* 숫자 페이지 번호들 (1 | 2 | 3 ...) */}
            <div className='page-numbers'>
                {pageNumbers.map((num) => (
                    <button key={num} onClick={() => onPageChange(num)} className={`page-num-btn ${page === num ? 'active' : ''}`}>
                        {num + 1}
                    </button>
                ))}
            </div>

            {/* 다음 그룹으로(>) */}
            <button className='nav-btn' onClick={() => onPageChange(Math.min(totalPages - 1, endPage))} disabled={endPage >= totalPages}>
                {">"}
            </button>
            {/* 맨 끝으로(>>) */}
            <button className='nav-btn' onClick={() => onPageChange(totalPages - 1)} disabled={page === totalPages - 1 || totalPages === 0}>
                {">>"}
            </button>
        </div>
    )
}

export default Pagination