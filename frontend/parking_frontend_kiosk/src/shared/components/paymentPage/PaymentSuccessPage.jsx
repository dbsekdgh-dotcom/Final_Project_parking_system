
import { useEffect, useRef, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { usePayment } from "../../hooks/usePaymentMutation";
import useVehicleStore from "../../../store/useVehicleStore";
import { requestAfterPayment } from "../../api/VehicleApi";

export function PaymentSuccessPage() {
  const [searchParams] = useSearchParams();
  const {afterMutation}=usePayment();
  const navigate=useNavigate();
  const hasCalled=useRef(false) //다시 랜더링되지 않도록
  const { paymentInfo } = useVehicleStore(); // 일반 출차 정보 호출
  const flowType=localStorage.getItem("paymentFlow") //로컬 스토리지에서 flowType 호출

  useEffect(() => {
    const processPayment= async()=>{
      if (hasCalled.current)return;

      const paymentKey=searchParams.get("paymentKey")
      const orderId=searchParams.get("orderId")
      const amount=searchParams.get("amount")
      const parkingLogId=localStorage.getItem("pendingParkingLogId")
      

      console.log(paymentKey)
      console.log(orderId)
      console.log(amount)
      console.log(parkingLogId)

      // 결제 성공 시
      if(paymentKey && orderId && amount && parkingLogId){
        const payload={
        "paymentKey":paymentKey,
        "orderId":orderId,
        "amount":amount,
        "parkingLogId":parkingLogId
        }
        hasCalled.current=true;

        if(flowType === "EXIT_GATE"){
          const afterResponse = await requestAfterPayment(payload);
          localStorage.removeItem("paymentFlow")
          navigate("/exit-departure",{
            state:{
              parkingLogId: Number(parkingLogId),
              message: afterResponse.message
            }
          })
        }else {
          await afterMutation.mutateAsync(payload)
        }

        
      }else{
        navigate("/PrepaymentResult",{
          state:{
              title : "정산 중 오류가 발생하였습니다.",
              subTitle : "결제 정보가 올바르지 않습니다. 다시 시도해주세요.",
              type: "error"
          }
        }) 
      }

    }
    processPayment()
    },[searchParams,afterMutation]);

  return (
    <div >
    </div>
  );
}