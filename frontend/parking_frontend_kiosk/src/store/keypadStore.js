import {create} from "zustand"

const useKeypadStore=create(set=>({
    carNumber:'',
    addCarNumber: (num)=>set(state=>({
        carNumber:state.carNumber.length<4? state.carNumber+num:state.carNumber
    })),
    deleteCarNumber: ()=>set(state=>({
        carNumber:state.carNumber.length>0? state.carNumber.slice(0,-1):state.carNumber
    })),
    resetCarNumber:()=>set({carNumber:''})   
}))
export default useKeypadStore;