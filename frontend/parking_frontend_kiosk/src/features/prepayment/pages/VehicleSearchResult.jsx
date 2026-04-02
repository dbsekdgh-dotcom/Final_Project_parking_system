import React, { useState } from 'react'
import '../../../app.css'
import './prepayment.css'
import useVehicleStore from '../../../store/useVehicleStore'
import { useQuery } from '@tanstack/react-query'
import {searchCar} from '../api/carNumberApi'
import { useNavigate } from 'react-router-dom'
import VehicleList from '../../../shared/components/vehicleList/VehicleList'

const VehicleSearchResult = () => {
  const {searchKeyword,setSelectedVehicle}=useVehicleStore();
  const navigate=useNavigate();

  const {data, isLoading, isError}=useQuery({
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
        <div className='page-container'>
            <h2 className='page-title'>검색결과</h2>
        </div>
        <div className='message-box'>
            {isLoading && <div className='loading-text'>차량정보 조회 중...</div>}
            {isError && <div className='error-text'>차량정보 조회중 오류가 발생하였습니다.</div>}
            {!isLoading && !isError && data && (<VehicleList vehicles={data} onSelect={selectHandler} />)}
        </div>
        <button type='button' className='back-button' onClick={()=>navigate(-1)}>
              돌아가기 
        </button>
    </div>
  )
}

export default VehicleSearchResult