import React, { useState } from 'react'
import '../../../app.css'
import './prepayment.css'
import useVehicleStore from '../../../store/useVehicleStore'
import { useQuery } from '@tanstack/react-query'
import {searchCar} from '../../../shared/api/VehicleApi'
import { useNavigate } from 'react-router-dom'
import VehicleList from '../../../shared/components/vehicleList/VehicleList'

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
                {isLoading && (
                    <div className='error-content-box'>
                        <div className='loading-spinner'>⚙️</div>
                        <p className='loading-message'>차량 정보를 조회 중입니다...</p>
                    </div>
                )}
                
                {/* 2. 에러 상태  */}
                {isError && (
                    <div className='error-content-box'>
                        <div className='error-icon'>⚠️</div>
                        <p className='error-message'>
                            {error?.response?.data?.message || "차량 정보를 가져오는 데 실패했습니다."}
                        </p>
                    </div>
                )}
                
                {/* 3. 성공 상태 */}
                {!isLoading && !isError && data && (
                    data.length > 0 ? (
                        <VehicleList vehicles={data} onSelect={selectHandler} />
                    ) : (
                        <div className='error-content-box'>
                            <div className='error-icon'>🚫</div>
                            <p className='loading-message'>검색 결과가 없습니다.</p>
                        </div>
                    )
                )}
            </div>

            {/* 공통 하단 버튼 */}
            <button type='button' className='back-button' onClick={() => navigate(-1)} disabled={isLoading}>
                돌아가기 
            </button>
        </div>
  )
}

export default VehicleSearchResult