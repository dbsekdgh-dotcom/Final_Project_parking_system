import React from 'react'
import VehicleInfo from '../../../shared/components/vehicleInfo/VehicleInfo'
import useVehicleStore from '../../../store/useVehicleStore';
import '../../../app.css'
import PaymentMethod from '../../../shared/components/paymentMethod/PaymentMethod';
import {  useQuery } from '@tanstack/react-query';
import { requestPayment } from '../../../shared/api/VehicleApi';
import { useNavigate } from 'react-router-dom';
import { usePayment } from '../../../shared/hooks/usePaymentMutation';

const SelectedVehicleInfo = () => {
    const {selectedVehicle, resetSearchKeyword, resetSelectedVehicle}=useVehicleStore();
    const {beforeMutation}=usePayment();

    
    const navigate = useNavigate();
    console.log("스토어에 저장된 원본 차량 정보:", selectedVehicle);

    const {data,isError, error ,isLoading}=useQuery({
        queryKey:['selectedVehicle',selectedVehicle],
        queryFn:async()=>await requestPayment(selectedVehicle),
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



    const paymentHandler=async(paymentData)=>{
        console.log("지금 결제",data)

        if(data.free){
            //결제할 요금이 없는 경우 
            navigate("/PrepaymentResult",{
            state:{
                title : "정산이 완료 되었습니다. ",
                subTitle : paymentData.message || "안전하게 출차해주세요.",
                type:"success"
                }
            })  
        }else{
            const settlementPayload={
                "parkingLogId":data.parkingLogId,
                "vehicleNumber":data.vehicleNumber,
                "usedPoint":paymentData.usedPoint,
                "paidAmount":paymentData.paidAmount,
                "settlementType":"PREPAYMENT"
            }
            beforeMutation.mutate(settlementPayload)

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
                <VehicleInfo vehicleNumber={data?.vehicleNumber} parkingTime={data?.parkingTime} fee={data?.amountToPay}/>
            </div>
            <div>
                <PaymentMethod userPoint={selectedVehicle?.userPoint} fee={data?.amountToPay} onConfirm={paymentHandler}/> 
            </div>
        </div>
    </div>
    
  )
}

export default SelectedVehicleInfo;