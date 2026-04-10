import { loadTossPayments, ANONYMOUS } from "@tosspayments/tosspayments-sdk";
import { useEffect, useState } from "react";
import useVehicleStore from "../../../store/useVehicleStore";

const clientKey = import.meta.env.VITE_TOSS_CLIENT_KEY;

export function PaymentPage() {
    const {paymentInfo}=useVehicleStore();
    const [ready, setReady] = useState(false);
    const [widgets, setWidgets] = useState(null);
    const customerKey=paymentInfo?.userEmail?paymentInfo.userEmail:ANONYMOUS
    const amount=paymentInfo?.amount?Math.floor(Number(paymentInfo.amount)):0;

    console.log(paymentInfo)

    useEffect(() => {
        if(!paymentInfo || amount <=0 )return;
        async function fetchPaymentWidgets() {
        // ------  결제위젯 초기화 ------
        const tossPayments = await loadTossPayments(clientKey);
        const widgets = tossPayments.widgets({ customerKey: customerKey });
        setWidgets(widgets);
        }
    fetchPaymentWidgets();
    }, [clientKey, customerKey,amount]);

    useEffect(() => {
        async function renderPaymentWidgets() {
            if (widgets == null) return;

            // ------ 주문의 결제 금액 설정 ------
            await widgets.setAmount({
                value:amount,
                currency:"KRW"
            });
            
            await Promise.all([
                // ------  결제 UI 렌더링 ------
                widgets.renderPaymentMethods({
                    selector: "#payment-method",
                    variantKey: "DEFAULT",
                }),
                // ------  이용약관 UI 렌더링 ------
                widgets.renderAgreement({
                    selector: "#agreement",
                    variantKey: "AGREEMENT",
                }),
            ]);
            setReady(true);
        }
        renderPaymentWidgets();
    }, [widgets,amount]);


    return (
    <div className="wrapper">
        <div className="box_section">
        {/* 결제 UI */}
        <div id="payment-method" />
        {/* 이용약관 UI */}
        <div id="agreement" />

        {/* 결제하기 버튼 */}
        <button
            className="button"
            disabled={!ready}
            onClick={async () => {
            try {
                await widgets.requestPayment({
                orderId: paymentInfo.orderId,
                orderName: paymentInfo.orderName,
                successUrl: window.location.origin + "payment/success",
                failUrl: window.location.origin + "payment/fail",
                customerEmail: paymentInfo.userEmail || undefined,
                customerName: paymentInfo.vehicleNumber
                });
            } catch (error) {
                // 에러 처리하기
                console.error(error);
            }
            }}
        >
            결제하기
        </button>
        </div>
    </div>
    );
}