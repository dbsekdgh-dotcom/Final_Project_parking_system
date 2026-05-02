import React, { useEffect, useState } from 'react'
import VehicleInfo from '../../../shared/components/vehicleInfo/VehicleInfo'
import ResultView from '../../../shared/components/resultView/ResultView'
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
    const [isPaymentLoading, setIsPaymentLoading]=useState(false)
    
    const navigate = useNavigate();
    console.log("스토어에 저장된 원본 차량 정보:", selectedVehicle);

    const {data,isError, error ,isLoading}=useQuery({
        queryKey:['selectedVehicle',selectedVehicle],
        queryFn:async()=>await requestPayment(selectedVehicle),
        enabled: !!selectedVehicle
    })  

    const homeHandler=()=>{
        resetSearchKeyword()
        resetSelectedVehicle()
        navigate('/')
    }

    const paymentHandler=async(paymentData)=>{
        console.log("지금 결제",data)
        if(isPaymentLoading)return;

        if(data?.free){
            navigate("/PrepaymentResult",{
                 state:{                                                                                                                                                               
                    title:"무료 출차 가능합니다.",                                                                                                                                    
                    subTitle: data.message || "이미 등록되었거나 정산할 금액이 없는 차량입니다.",                                                                                                                   
                    type:"success"                                                                                                                                                    
                }   
            })
            return
        }

        setIsPaymentLoading(true)

        const settlementPayload={
            "parkingLogId":data.parkingLogId,
            "vehicleNumber":data.vehicleNumber,
            "usedPoint":paymentData.usedPoint || 0,
            "paidAmount":paymentData.paidAmount ||0,
            "settlementType":"PREPAYMENT",
        }
        await beforeMutation.mutateAsync(settlementPayload)
        setIsPaymentLoading(false)
    }
    

    if (!selectedVehicle) {
        return <ResultView title="조회 정보 없음" subTitle="선택된 차량 정보가 없습니다." type="error"/>
    }

    // {/* 1. 로딩 상태 */}
    if (isLoading) {
        return <ResultView title="정보 조회 중" subTitle="정산 데이터를 불러오고 있습니다." type="loading" />
    }

    // {/* 2. 에러 상태 */}
    if (isError) {
        resetSelectedVehicle()
        return<ResultView title="조회 실패" subTitle={error?.message || "서버와의 통신이 원활하지 않습니다."} type="error" />
    }
    
    // {/* 3. 성공 상태 */}
  return (
    <div className='full-page-container'>
        <h2 className='page-title'>결제 확인</h2>
        <button className='header-back-button' onClick={homeHandler}>처음으로</button>
        <div className='selected-vehicle-info'>
            <div>
                <VehicleInfo vehicleNumber={data?.vehicleNumber} parkingTime={data?.parkingTime} fee={data?.amountToPay}/>
            </div>
            <div>
                <PaymentMethod userPoint={selectedVehicle?.userPoint} fee={data?.amountToPay} onConfirm={paymentHandler} isLoading={isPaymentLoading}/> 
            </div>
        </div>
    </div>
    
  )
}

export default SelectedVehicleInfo;