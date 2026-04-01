import React from 'react'
import VehicleInfo from '../../../shared/components/vehicleInfo/VehicleInfo'
import useVehicleStore from '../../../store/useVehicleStore';
import '../../../app.css'
import PaymentMethod from '../../../shared/components/paymentMethod/PaymentMethod';

const SelectedVehicleInfo = () => {
    const {selectedVehicle}=useVehicleStore();

  return (
    <div className='full-page-container'>
        <p className='payment-subtitle'>결제 확인</p>
        <div className='selectedVehicleInfo'>
            <div>
                <VehicleInfo vehicleNumber={selectedVehicle} parkingTime={30} fee={5000}/>
            </div>
            <div>
            <PaymentMethod userPoint={3000} fee={5000}/> 
            </div>
        </div>
    </div>
  )
}

export default SelectedVehicleInfo