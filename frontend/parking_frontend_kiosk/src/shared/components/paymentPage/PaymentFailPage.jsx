import { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";

export function PaymentFailPage() {
  const [searchParams] = useSearchParams();
  const navigate=useNavigate();

  useEffect(()=>{
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