import {create} from "zustand"

const useVehicleStore=create(set=>({
    searchKeyword:'',
    selectedVehicle:null,
    paymentInfo:null,

    //keypad 입력 시 저장될 데이터
    addSearchKeyword: (vehicleNumber)=>set(state=>({
        searchKeyword:state.searchKeyword.length<4? state.searchKeyword+vehicleNumber:state.searchKeyword
    })),
    deleteSearchKeyword: ()=>set(state=>({
        searchKeyword:state.searchKeyword.length>0? state.searchKeyword.slice(0,-1):state.searchKeyword
    })),
    resetSearchKeyword:()=>set({searchKeyword:''}),

    //사용자가 차량 선택시 저장될 데이터(parking_log_id,vehicleNumber)
    setSelectedVehicle:(vehicle)=>set({selectedVehicle:vehicle}),
    resetSelectedVehicle : ()=>set({selectedVehicle:null}),
    
    //결제 요청정보 
    setPaymentInfo:(info)=>set({paymentInfo:info}),

    //저장정보 전부 삭제
    resetAll:()=>{
        set({searchKeyword:''}),
        set({selectedVehicle:null}),
        set({paymentInfo:null})
    }
}))
export default useVehicleStore;