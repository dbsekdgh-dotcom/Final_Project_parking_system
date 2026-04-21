import axios from "axios";
import adminApi from "../../../shared/api/adminApi";

//현재 적용 중인 요금 정책 조회
export const searchFeePolicy=async()=>{
    const res=await adminApi.get(`/fee-policy`);
    console.log("요금정책==>",res.data)
    return res.data;
}

//요금 정책 수정
export const changeFeePolicy=async(changePolicy)=>{
    const res=await adminApi.post(`/fee-policy/change`,changePolicy)
    console.log("수정하려는 요금 정책==>",res.data)
    return res;
}
//요금 정책 업데이트
export const updateFeePolicy=async(updatePolicy)=>{
    const res=await adminApi.post(`/fee-policy/update`,updatePolicy)
    console.log("수정하려는 요금 정책==>",res.data)
    return res;
}
//할인권 정책 삭제
export const deleteTicketPolicy=async(ticketPolicyId)=>{
    console.log("삭제하려는 티켓 아이디 ==>",ticketPolicyId)
    const res=await adminApi.delete(`/ticket-policy`,{params:{ticketPolicyId}})
    return res;
}
//할인권 등록
export const insertTicketPolicy=async(ticketPolicies)=>{
    console.log("등록하려는 티켓 정보 ==>",ticketPolicies)
    const res=await adminApi.post(`/ticket-policy`,ticketPolicies)
    return res;
}