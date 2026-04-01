import {create} from "zustand"

const useVehicleStore=create(set=>({
    searchKeyword:'',
    selectedVehicle:'',
    addSearchKeyword: (vehicleNumber)=>set(state=>({
        searchKeyword:state.searchKeyword.length<4? state.searchKeyword+vehicleNumber:state.searchKeyword
    })),
    deleteSearchKeyword: ()=>set(state=>({
        searchKeyword:state.searchKeyword.length>0? state.searchKeyword.slice(0,-1):state.searchKeyword
    })),
    resetSearchKeyword:()=>set({searchKeyword:''})   ,
    setSelectedVehicle:(vehicleNumber)=>set({selectedVehicle:vehicleNumber})
}))
export default useVehicleStore;