import React, { useEffect, useState } from 'react'
import './ParkingLogPage.css'
import { getParkingLogDetail, getParkingLogList, getParkingLogSummary } from '../api/parkingLogApi'
import ParkingSummaryCards from '../components/ParkingSummaryCards'
import ParkingLogTable from '../components/ParkingLogTable'

const ParkingLogPage = () => {
    const [summaryData, setSummaryData] = useState(null) //상단 요약 목록
    const [parkingLogs, setParkingLogs] = useState([]) //하단 테이블 목록
    const [keyword, setKeyword] = useState('') //입력중인 검색어
    const [searchQuery, setSearchQuery] = useState('') //실제 검색에 사용될 키워드
    const [page, setPage] = useState(0) //현재 페이지
    const [totalPages, setTotalPages] = useState(0)
    const [filterStatus,setFilterStatus]=useState('ALL') //카드 클릭시 적용할 필터 상태 (예:'ALL','RESIDENT','VISIT'등)

    useEffect(() => { // 요약 정보는 마운트시 1번만 데이터 호출
        //API 호출해서 데이터 가져오기
        getParkingLogSummary()
            .then(data => {
                console.log("백엔드에서 온 데이터:", data)
                setSummaryData(data);
            })
            .catch(err => console.error("요약 정보 호출 에러:", err))
    }, []);

    //목록 정보 - 검색어나 페이지가 바뀔때마다 데이터 호출
    useEffect(() => {
        console.log("필터 적용 요청:",filterStatus)
        getParkingLogList(searchQuery, page, filterStatus)
            .then(data => {
                setParkingLogs(data.content);
                setTotalPages(data.totalPages);
            }).catch(err=>console.error("목록 호출 에러:",err))
    }, [searchQuery, page, filterStatus]);

    //검색버튼 핸들러
    const handleSearch=(e)=>{
        e.preventDefault();
        console.log("검색버튼 클릭, 키워드:",keyword)
        setSearchQuery(keyword) //현재 입력된 값을 검색 쿼리로 확정
        setPage(0); //검색시 첫페이지로 이동
    }

    const handleCardClick=(status)=>{
        setFilterStatus(status)
        setPage(0) //필터 변경시 첫페이지로
        setKeyword('')
        setSearchQuery('')

        console.log(`${status} 필터 적용 및 검색어 초기화`)
    }

    const handleShowDetail=(id)=>{
        console.log("선택된 ID:",id)
        getParkingLogDetail(id)
    }

    return (
        <div className='parking-log-page' style={{ padding: '20px' }}>
            {/* 상단 카드 컴포넌트에 데이터 전달 */}
            <ParkingSummaryCards data={summaryData} onCardClick={handleCardClick} activeFilter={filterStatus}/>

            {/* 검색 영역 */}
            <section className='search-section' style={{margin:'20px 0'}}>
                <form className='search-form' onSubmit={handleSearch}>
                    <input className='search-input' type="text" placeholder='차량번호 검색 (예: 12가 3456)' value={keyword} onChange={(e)=>setKeyword(e.target.value)}/>
                    <button className='search-button' type='submit'>조회</button>
                </form>
            </section>

            {/* 하단 테이블 영역 */}
            <ParkingLogTable 
            logs={parkingLogs} page={page} totalPages={totalPages} onPageChange={setPage} onShowDetail={handleShowDetail}/>
        </div>
    )
}

export default ParkingLogPage 