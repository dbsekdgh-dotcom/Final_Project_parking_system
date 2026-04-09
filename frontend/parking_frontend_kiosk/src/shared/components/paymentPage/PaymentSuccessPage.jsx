
import { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import useVehicleStore from "../../../store/useVehicleStore";
import { usePayment } from "../../hooks/usePaymentMutation";

export function PaymentSuccessPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const {paymentInfo}=useVehicleStore();
  const {afterMutation}=usePayment();
  useEffect(() => {
    //백엔드 승인 요청

    // 결제 성공 시
    const payload={
    "paymentKey":searchParams.get("paymentKey"),
    "orderId":searchParams.get("orderId"),
    "amount":searchParams.get("amount"),
    "parkingLogId":paymentInfo?.parkingLogId
    }
    afterMutation.mutate(payload)
    },[]);

  return (
    <div className="result wrapper">
      <div className="box_section">
        <h2>
          결제 성공
        </h2>
        <p>{`주문번호: ${searchParams.get("orderId")}`}</p>
        <p>{`결제 금액: ${Number(
          searchParams.get("amount")
        ).toLocaleString()}원`}</p>
        <p>{`paymentKey: ${searchParams.get("paymentKey")}`}</p>
      </div>
    </div>
  );
}