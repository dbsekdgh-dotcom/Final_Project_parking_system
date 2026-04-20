import { createSlice,createAsyncThunk } from "@reduxjs/toolkit";
import { controlParkingSpace } from "../api/parkingSpaceApi";


export const __controlParkingSpace = createAsyncThunk(
    "parkingSpace/control",
    async ({spaceId,action}, thunkAPI) => {
        try{
            const response= await controlParkingSpace(spaceId,action)
            return response.data
        }catch(error){
            const errorMessage = error.response?.data?.message || "상태 변경에 실패했습니다."
            return thunkAPI.rejectWithValue(errorMessage)
        }
    }
)

//슬라이스(장부)설정
const parkingSpaceSlice = createSlice({
    name: 'parkingSpace',
    initialState:{
        loading: false,
        error: null,
        successMessage: null,
    },
    reducers: {
        //메시지 상태 초기화가 필요할 때 호출
        clearStatus: (state)=>{
            state.error = null;
            state.successMessage=null;
        }
    },
    extraReducers: (builder) => {
        builder
        //요청시작
        .addCase(__controlParkingSpace.pending,(state)=>{
            state.loading=true
            state.error=null
            state.successMessage=null
        })
        //요청 성공
        .addCase(__controlParkingSpace.fulfilled,(state,action)=>{
            state.loading=false
            state.successMessage=action.payload 
        })
        //요청 실패
        .addCase(__controlParkingSpace.rejected,(state,action)=>{
            state.loading=false
            state.error=action.payload //에러 메시지 저장
        })
    }
})

export const {clearStatus}=parkingSpaceSlice.actions
export default parkingSpaceSlice.reducer