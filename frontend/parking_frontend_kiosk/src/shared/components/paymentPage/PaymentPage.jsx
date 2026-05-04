import { loadTossPayments, ANONYMOUS } from "@tosspayments/tosspayments-sdk";
import { useEffect, useState } from "react";
import useVehicleStore from "../../../store/useVehicleStore";
import { useNavigate } from "react-router-dom";
import './paymentPage.css'

const clientKey = import.meta.env.VITE_TOSS_CLIENT_KEY;

export function PaymentPage() {
    const {paymentInfo}=useVehicleStore();
    const [ready, setReady] = useState(false);
    const [widgets, setWidgets] = useState(null);
    const customerKey=paymentInfo?.userEmail?paymentInfo.userEmail:ANONYMOUS
    const amount=paymentInfo?.amount?Math.floor(Number(paymentInfo.amount)):0;
    const navigate=useNavigate();


    console.log(paymentInfo)

    useEffect(() => {
        if(!paymentInfo || amount <=0 )return;
        async function fetchPaymentWidgets() {
        // ------  결제위젯 초기화 ------
        console.log("로컬 키 값 확인:", clientKey);
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

    const paymentHandler=async()=>{
        try {
            //store정보 리셋되는 걸 방지
            if(paymentInfo?.parkingLogId){
                localStorage.setItem("pendingParkingLogId",paymentInfo?.parkingLogId)
            }

            await widgets.requestPayment({
            orderId: paymentInfo.orderId,
            orderName: paymentInfo.orderName,
            successUrl: `${window.location.origin}/payment/success`,
            failUrl: `${window.location.origin}/payment/fail`,
            customerEmail: paymentInfo.userEmail || undefined,
            customerName: paymentInfo.vehicleNumber
            });
        } catch (error) {
            // 에러 처리하기
            console.error(error);
            if(error.code==='USER_CANCEL'){
                navigate(`/payment/fail?code=${error.code}&message=${encodeURIComponent(error.message)}`);
            }
        }
    }

    return (
    <div className="payment-page-container">
        <div className="payment-card">
            {/* 주문정보 요약 */}
            <section className="order-summary">
                <div className="order-details">
                    <span className="order-label">결제할 차량</span>
                    <h2 className="order-title">{paymentInfo?.vehicleNumber || "차량 정보 없음"}</h2>
                </div>
                <div className="order-amount">
                    <span className="amount-text">{amount.toLocaleString()}원</span>
                </div>
            </section>
            {/* 토스 위젯 영역 */}
            <section className="widget-section">
                <h3 className="section-title">결제 수단 선택</h3>
                <div id="payment-method" className="toss-widget" />
                
                <div className="divider" />
                
                <h3 className="section-title">약관 동의</h3>
                <div id="agreement" className="toss-widget" />
            </section>
            {/* 하단 고정 버튼 구역 */}
            <footer className="payment-action">
                <button 
                    className="payment-button" 
                    disabled={!ready} 
                    onClick={paymentHandler}
                >
                    {ready ? `${amount.toLocaleString()}원 결제하기` : "결제창 불러오는 중..."}
                </button>
            </footer>
        </div>
    </div>
    );
}