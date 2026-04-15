import axios from "axios";
import adminApi from "../../../shared/api/adminApi";

//현재 적용 중인 요금 정책 조회
export const searchFeePolicy=async()=>{
    const res=await adminApi.get(`/fee-policy`);
    console.log("요금정책==>",res.data)
    return res.data;
}