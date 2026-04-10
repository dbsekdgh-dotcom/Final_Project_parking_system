
import { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { usePayment } from "../../hooks/usePaymentMutation";

export function PaymentSuccessPage() {
  const [searchParams] = useSearchParams();
  const {afterMutation}=usePayment();
  const navigate=useNavigate();

  useEffect(async() => {
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
      await afterMutation.mutateAsync(payload)
    }else{
      navigate("/PrepaymentResult",{
        state:{
            title : "정산 중 오류가 발생하였습니다.",
            subTitle : "결제 정보가 올바르지 않습니다. 다시 시도해주세요.",
            type: "error"
        }
      }) 
    }
    },[searchParams,afterMutation]);

  return (
    <div >
    </div>
  );
}