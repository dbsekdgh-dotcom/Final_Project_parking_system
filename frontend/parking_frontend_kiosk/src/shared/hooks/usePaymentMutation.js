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
            navigate("/prepaymentSuccess",{
                state:{
                    title : "정산이 완료 되었습니다. ",
                    subTitle : afterResponse.message
                }
            }) 
        },
        onError:(error)=>{
            const message=error.message
            const code=error.code
            navigate(`/payment/fail?message=${error.message ||message}&code=${error.code ||code}`);
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
            const message=error.message
            const code=error.code
            navigate(`/payment/fail?message=${error.message ||message}&code=${error.code ||code}`);
        }
    })
    return {beforeMutation,afterMutation}
}