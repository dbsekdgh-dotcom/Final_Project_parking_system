import { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { cancelPayment } from "../../api/VehicleApi";

export function PaymentFailPage() {
  const [searchParams] = useSearchParams();
  const navigate=useNavigate();

  useEffect(()=>{
    const releaseLock=async()=>{
      const vehicleNumber=sessionStorage.getItem("pendingVehicleNumber");
      if(vehicleNumber){
        try{
          await cancelPayment(vehicleNumber);
        }catch(err){
          console.error("결제 락 해제 실패", err);
        }
      }
      sessionStorage.removeItem("pendingVehicleNumber");
      sessionStorage.removeItem("pendingParkingLogId");
      sessionStorage.removeItem("paymentFlow");
    };
    releaseLock();
    const errorMsg=searchParams.get("message") || "결제 중 알 수 없는 오류가 발생하였습니다."
      navigate("/PrepaymentResult",{
        state:{
            title : "정산 중 오류가 발생하였습니다.",
            subTitle : errorMsg,
            type: "error"
        }
     }) 
  },[searchParams])

  return (
    <div>

    </div>
  );
}