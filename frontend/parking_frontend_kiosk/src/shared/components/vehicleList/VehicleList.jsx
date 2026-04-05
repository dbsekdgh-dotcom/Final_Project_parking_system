import React from 'react'
import './vehicleList.css'

const VehicleList = ({vehicles,onSelect}) => {
    if(!Array.isArray(vehicles) || !vehicles || vehicles.length ==0){
        return <div className='empty-text'>조회된 차량이 없습니다.</div>;
    }

    return (
        <div>
            {
                vehicles.map((v,index)=> {
                return <div className='vehicle-card' onClick={()=>onSelect(v)} ontouchstart={()=>{}} key={index}>
                    <span type='button'  className='vehicle-number'>{v.vehicleNumber}</span>
                    <span className='arrow-icon'>→</span>
                    </div>
                })
            }
        </div>
    )
}

export default VehicleList