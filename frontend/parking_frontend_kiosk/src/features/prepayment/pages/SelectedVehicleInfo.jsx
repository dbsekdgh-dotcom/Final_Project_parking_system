import React from 'react'
import VehicleInfo from '../../../shared/components/vehicleInfo/VehicleInfo'
import useVehicleStore from '../../../store/useVehicleStore';
import '../../../app.css'
import PaymentMethod from '../../../shared/components/paymentMethod/PaymentMethod';
import { useQuery } from '@tanstack/react-query';
import { requestPrepayment } from '../../../shared/api/VehicleApi';

const SelectedVehicleInfo = () => {
    const {selectedVehicle}=useVehicleStore();
    
    if(!selectedVehicle){
        return <div className='error-page'>선택된 차량 정보가 없습니다.</div>
    }

    const {data,isError, error ,isLoading}=useQuery({
        queryKey:['selectedVehicle',selectedVehicle],
        queryFn:async()=>await requestPrepayment(selectedVehicle),
        enabled: !!selectedVehicle
    })
    if(isLoading){
        return <div className='loading-full'>정보를 불러오는 중입니다...</div>
    }

    if(isError){
        return(
            <div className='full-page-container error-page'>
                <div className='error-content'>
                    <div className='error-icon'>⚠️</div>
                    <p className='error-message'>
                        {error?.message || "차량정보를 가져오는 데 실패했습니다."}
                    </p>
                    <button className='back-to-list-btn'>
                        onClick={()=>navigate('/prepayment')}
                    차량 검색 화면으로 이동
                    </button>
                </div>
            </div>
        )
    }
    
  return (
    <div className='full-page-container'>
        <p className='payment-subtitle'>결제 확인</p>
        <div className='selectedVehicleInfo'>
            <div>
                <VehicleInfo vehicleNumber={selectedVehicle?.vehicleNumber} parkingTime={30} fee={5000}/>
            </div>
            <div>
            <PaymentMethod userPoint={3000} fee={5000}/> 
            </div>
        </div>
    </div>
  )
}

export default SelectedVehicleInfo;