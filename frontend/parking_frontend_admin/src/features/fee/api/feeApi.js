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
//할인권 비활성화
export const inactivateTicketPolicy=async(ticketPolicyId)=>{
    console.log("수정하려는 티켓 아이디 ==>",ticketPolicyId)
    const res=await adminApi.put(`/ticket-policy`,{ticketPolicyId})
    return res;
}
//과거 요금 정책 이력 불러오기
export const searchPolicyHistory=async()=>{
    const res=await adminApi.get(`/fee-policy/history`)
    console.log("요금정책이력==>",res.data)
    return res.data
}
//현금흐름 자료 불러오기
export const getStats=async(payload)=>{
    const res=await adminApi.get(`/stats`,{params:payload})
    console.log("현금흐름자료==> ",res.data)
    return res.data
}
//매출 분석 자료 불러오기
export const getAnalysis=async(payload)=>{
    const res=await adminApi.get(`/stats/analysis`,{params:payload})
    console.log("매출 분석 자료==>",res.data)
    return res.data
}