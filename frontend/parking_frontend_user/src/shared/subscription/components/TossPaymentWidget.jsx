import { useEffect, useRef, useState } from 'react';
import { loadTossPayments, ANONYMOUS } from '@tosspayments/tosspayments-sdk';

const CLIENT_KEY = import.meta.env.VITE_TOSS_CLIENT_KEY;

export default function TossPaymentWidget({ paidAmount, days, orderId, onReady }) {
    const widgetsRef = useRef(null);
    const [ready, setReady] = useState(false);

    useEffect(() => {
        let cancelled = false;

        async function init() {
            const tossPayments = await loadTossPayments(CLIENT_KEY);
            if (cancelled) return;

            const widgets = tossPayments.widgets({ customerKey: ANONYMOUS });
            widgetsRef.current = widgets;

            await widgets.setAmount({ value: paidAmount, currency: 'KRW' });
            if (cancelled) return;

            await Promise.all([
                widgets.renderPaymentMethods({ selector: '#sub-payment-method', variantKey: 'DEFAULT' }),
                widgets.renderAgreement({ selector: '#sub-agreement', variantKey: 'AGREEMENT' }),
            ]);
            if (cancelled) return;

            setReady(true);
            onReady(widgets);
        }
        init();

        return () => {
            cancelled = true;
            widgetsRef.current = null;
            const methodEl = document.getElementById('sub-payment-method');
            const agreementEl = document.getElementById('sub-agreement');
            if (methodEl) methodEl.innerHTML = '';
            if (agreementEl) agreementEl.innerHTML = '';
        };
    }, []);

    return (
        <div className="sub-toss-area">
            <div id="sub-payment-method" />
            <div id="sub-agreement" />
            <button
                className="sub-modal-btn-primary"
                disabled={!ready}
                onClick={async () => {
                    if (!widgetsRef.current) return;
                    try {
                        await widgetsRef.current.requestPayment({
                            orderId,
                            orderName: `정기권 ${days}일`,
                            successUrl: `${window.location.origin}/subscription/success`,
                            failUrl: `${window.location.origin}/subscription/fail`,
                        });
                    } catch (err) {
                        if (err.code !== 'USER_CANCEL') console.error(err);
                    }
                }}
            >
                결제하기
            </button>
        </div>
    );
}
