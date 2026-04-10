import { useMutation } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import useVehicleStore from "../../store/useVehicleStore";
import { requestBeforePayment,requestAfterPayment } from "../api/VehicleApi";

export const usePayment=()=>{
    const {setPaymentInfo}=useVehicleStore();
    const navigate=useNavigate();

    const afterMutation=useMutation({
        mutationFn:async(payload)=>{
            return await requestAfterPayment(payload);
        },
        onSuccess:(afterResponse)=>{
            navigate("/PrepaymentResult",{
                state:{
                    title : "정산이 완료 되었습니다. ",
                    subTitle : afterResponse.message,
                    type: "success"
                }
            }) 
        },
        onError:(error)=>{
            navigate("/PrepaymentResult",{
                state:{
                    title : "정산 중 오류가 발생하였습니다.",
                    subTitle : error.message || "잠시 후 다시 시도해주세요.",
                    type: "error"
                }
            }) 
        }
    })
    const beforeMutation=useMutation({
        mutationFn:async(payload)=>{
            return await requestBeforePayment(payload);
        },
        onSuccess:(beforeResponse)=>{
            if(beforeResponse.paymentRequired){
                //토스 페이먼츠 요청 정보 저장
                setPaymentInfo(beforeResponse)
                //토스 페이먼츠 요청
                navigate("/payment")
                return
            }else{
                const payload={
                "paymentKey":"POINT_FULL_PAYMENT",
                "orderId":beforeResponse.orderId,
                "amount":beforeResponse.amount,
                "parkingLogId":beforeResponse.parkingLogId
                }
                afterMutation.mutate(payload)
            }
        },
        onError:(error)=>{
            navigate("/PrepaymentResult",{
                state:{
                    title : "정산 중 오류가 발생하였습니다.",
                    subTitle : error.message || "잠시 후 다시 시도해주세요.",
                    type: "error"
                }
            }) 
        }
    })
    return {beforeMutation,afterMutation}
}