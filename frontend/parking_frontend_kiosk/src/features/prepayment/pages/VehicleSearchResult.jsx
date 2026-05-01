import React, { useState } from 'react'
import '../../../app.css'
import './prepayment.css'
import useVehicleStore from '../../../store/useVehicleStore'
import { useQuery } from '@tanstack/react-query'
import {searchCar} from '../../../shared/api/VehicleApi'
import { useNavigate } from 'react-router-dom'
import VehicleList from '../../../shared/components/vehicleList/VehicleList'
import ResultView from '../../../shared/components/resultView/ResultView'

const VehicleSearchResult = () => {
  const {searchKeyword,setSelectedVehicle}=useVehicleStore();
  const navigate=useNavigate();

  const {data, isLoading, isError, error}=useQuery({
    queryKey: ['vehicle',searchKeyword],
    queryFn: async()=> await searchCar(searchKeyword),
    enabled: !!searchKeyword
  });

  const selectHandler=(vehicle)=>{
    setSelectedVehicle(vehicle)
    navigate("/selectedVehicle")
  }

  return (
<div className='full-page-container'>
            <div className='page-header-container'>
                <h2 className='page-title'>검색결과</h2>
            </div>
            
            <div className='content-area'>
                {/* 1. 로딩 상태 (디자인 통일) */}
                {isLoading && 
                    <ResultView 
                        title="조회 중" 
                        subTitle="차량 정보를 가져오고 있습니다..." 
                        type="loading" 
                    />
                }
                
                {/* 2. 에러 상태  */}
                {isError && 
                    <ResultView 
                        title="조회 실패" 
                        subTitle={error?.response?.data?.message || "차량 정보를 가져오는 데 실패했습니다."} 
                        type="error" 
                    />
                }
                
                {/* 3. 검색 결과 없음 */}
                if (data && data.length === 0) {
                    <ResultView 
                        title="검색 결과 없음" 
                        subTitle={`${searchKeyword}에 대한 입차 기록이 없습니다.`} 
                        type="error" 
                    />
                }

                {/* 3. 성공 상태 */}
                {!isLoading && !isError && data &&<VehicleList vehicles={data} onSelect={selectHandler} />}
            </div>

            {/* 공통 하단 버튼 */}
            <button type='button' className='back-button' onClick={() => navigate(-1)} disabled={isLoading}>
                돌아가기 
            </button>
        </div>
  )
}

export default VehicleSearchResult