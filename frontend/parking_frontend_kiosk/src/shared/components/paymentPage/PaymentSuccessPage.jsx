
import { useEffect, useRef, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { usePayment } from "../../hooks/usePaymentMutation";
import useVehicleStore from "../../../store/useVehicleStore";
import { requestAfterPayment } from "../../api/VehicleApi";
import { purchaseConfirm } from "../../../features/store/api/StoreApi";

export function PaymentSuccessPage() {
  const [searchParams] = useSearchParams();
  const {afterMutation}=usePayment();
  const navigate=useNavigate();
  const hasCalled=useRef(false) //다시 랜더링되지 않도록
  const { paymentInfo } = useVehicleStore(); // 일반 출차 정보 호출
  const flowType=localStorage.getItem("paymentFlow") //로컬 스토리지에서 flowType 호출

  useEffect(() => {
    const processPayment = async () => {
      if (hasCalled.current) return;
      hasCalled.current = true;

      const paymentKey = searchParams.get("paymentKey");
      const orderId = searchParams.get("orderId");
      const amount = searchParams.get("amount");

      if (!paymentKey || !orderId || !amount) {
          navigate("/PrepaymentResult", {
              state: {
                  title: "정산 중 오류가 발생하였습니다.",
                  subTitle: "결제 정보가 올바르지 않습니다. 다시 시도해주세요.",
                  type: "error"
              }
          });
          return;
      }

      if (flowType === 'STORE_TICKET') {
          const ticketPolicyId = localStorage.getItem('pendingTicketPolicyId');
          const quantity = Number(localStorage.getItem('pendingQuantity') || '1');
          try
          {
          await purchaseConfirm(ticketPolicyId, quantity);
          localStorage.removeItem('paymentFlow');
          localStorage.removeItem('pendingTicketPolicyId');
          localStorage.removeItem('pendingQuantity');
          navigate('/store/purchase/complete');
        } catch(e){
          localStorage.removeItem('paymentFlow');
          localStorage.removeItem('pendingTicketPolicyId');
          localStorage.removeItem('pendingQuantity');
          navigate('/store/main',{
            state: { error: '구매 확인 중 오류가 발생했습니다.'}
          });
        }
          return;
      }

      const parkingLogIdRaw = localStorage.getItem("pendingParkingLogId");
      const payload = { paymentKey, orderId, amount, parkingLogId: parkingLogIdRaw };

      if (flowType === "EXIT_GATE") {
          const exitPayload ={
            paymentKey,
            orderId,
            amount: Number(amount),
            parkingLogId: Number(parkingLogIdRaw)
          };
          const afterResponse = await requestAfterPayment(exitPayload);
          localStorage.removeItem("paymentFlow");
          navigate("/exit-departure",{
            state: {
              parkingLogId : Number(parkingLogIdRaw),
              message: afterResponse.message
            }
          });
          return;
      }

      await afterMutation.mutateAsync(payload);
  };
    processPayment()
    },[searchParams,afterMutation]);

  return (
    <div >
    </div>
  );
}