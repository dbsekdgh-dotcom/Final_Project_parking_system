import React from 'react'
import VehicleInfo from '../../../shared/components/vehicleInfo/VehicleInfo'
import useVehicleStore from '../../../store/useVehicleStore';
import '../../../app.css'
import PaymentMethod from '../../../shared/components/paymentMethod/PaymentMethod';
import { useQuery } from '@tanstack/react-query';
import { requestPrepayment } from '../../../shared/api/VehicleApi';
import { useNavigate } from 'react-router-dom';

const SelectedVehicleInfo = () => {
    const {selectedVehicle, resetSearchKeyword, resetSelectedVehicle}=useVehicleStore();
    const navigate = useNavigate();

    const {data,isError, error ,isLoading}=useQuery({
        queryKey:['selectedVehicle',selectedVehicle],
        queryFn:async()=>await requestPrepayment(selectedVehicle),
        enabled: !!selectedVehicle
    })  
    
    const searchCarHandler=()=>{
        resetSelectedVehicle()
        navigate('/prepayment')
    }

    const homeHandler=()=>{
        resetSearchKeyword()
        resetSelectedVehicle()
        navigate('/')
    }

    const paymentHandler=(paymentData)=>{
        const settlementPayload={
            "parkingLogId":data.parkingLogId,
            "vehicleNumber":data.vehicleNumber,
            "usedPoint":paymentData.usedPoint,
            "paidAmount":paymentData.paidAmount,
        }
        if(paymentData.paidAmount==0){
            //바로 DB호출
        }else{
            //토스 페이먼츠 호출 후 db호출
        }
    }

    if (!selectedVehicle) {
        return (
            <div className='full-page-container'>
                <div className='error-content-box'>
                    <div className='error-icon'>🔍</div>
                    <p className='error-message'>선택된 차량 정보가 없습니다.</p>
                    <button className='error-back-btn' onClick={() => navigate('/prepayment')}>
                        차량 검색 화면으로 이동
                    </button>
                </div>
            </div>
        )
    }

    {/* 1. 로딩 상태 */}
    if (isLoading) {
        return (
            <div className='full-page-container'>
                <div className='error-content-box'>
                    <div className='loading-spinner'>⚙️</div> 
                    <p className='loading-message'>정보를 불러오는 중입니다...</p>
                </div>
            </div>
        );
    }

    {/* 2. 에러 상태 */}
    if (isError) {
        return (
            <div className='full-page-container'>
                <div className='error-content-box'>
                    <div className='error-icon'>⚠️</div>
                    <p className='error-message'>
                        {error?.message || "차량 정보를 가져오는 데 실패했습니다."}
                    </p>
                    <button className='error-back-btn' onClick={searchCarHandler}>차량 검색 화면으로 이동</button>
                </div>
            </div>
        );
    }
    
    {/* 3. 성공 상태 */}
  return (
    <div className='full-page-container'>
        <h2 className='page-title'>결제 확인</h2>
        <button className='header-back-button' onClick={homeHandler}>처음으로</button>
        <div className='selected-vehicle-info'>
            <div>
                <VehicleInfo vehicleNumber={data?.vehicleNumber} parkingTime={data?.parkingTime} fee={data?.calculatedFee}/>
            </div>
            <div>
                <PaymentMethod userPoint={selectedVehicle?.userPoint} fee={data?.calculatedFee} onConfirm={paymentHandler}/> 
            </div>
        </div>
    </div>
    
  )
}

export default SelectedVehicleInfo;