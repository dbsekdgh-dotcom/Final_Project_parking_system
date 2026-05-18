import { useNavigate } from "react-router-dom";
import useVehicleStore from "../../../store/useVehicleStore";
import useStoreStore from "../../../store/useStoreStore";
import { useEffect, useState } from "react";
import { getTicketPolicies, getWallets, purchaseReady } from "../api/StoreApi";
import './TicketPuchasePage.css';

export default function TicketPurchasePage() {
    const navigate = useNavigate();
    const { setPaymentInfo } = useVehicleStore();
    const { wallets, setWallets } = useStoreStore();
    const [policies, setPolicies] = useState([]);
    const [selectedId, setSelectedId] = useState(null);
    const [quantity, setQuantity] = useState(1);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        const load = async () => {
            const [pList, wList] = await Promise.all([getTicketPolicies(), getWallets()]);
            setPolicies(pList);
            setWallets(wList);
        };
        load();
    }, []);

    const getRemaining = (id) => {
        const w = wallets.find(w => w.ticketPolicyId === id);
        return w ? w.remainingCount : 0;
    };

    const handlePurchase = async () => {
        if (!selectedId || loading) return;
        setLoading(true);
        try {
            const res = await purchaseReady(selectedId, quantity);
            sessionStorage.setItem('paymentFlow', 'STORE_TICKET');
            sessionStorage.setItem('pendingTicketPolicyId', String(selectedId));
            sessionStorage.setItem('pendingQuantity', String(quantity));
            setPaymentInfo({
                orderId: res.orderId,
                orderName: res.orderName,
                amount: res.amount,
                userEmail: null,
                vehicleNumber: '할인권 구매',
            });
            navigate('/payment');
        } catch {
            alert('구매 요청 중 오류가 발생했습니다.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="ticket-purchase-wrapper">
                <div className="ticket-purchase-header">
                    <h2>할인권 구매</h2>
                    <button className="btn-back" onClick={() => navigate('/store/main')}>돌아가기</button>
                </div>

                <div className="ticket-purchase-card">
                    <h3>할인권 선택</h3>
                    {policies.map(policy => (
                        <div
                            key={policy.ticketPolicyId}
                            className={`policy-item${selectedId === policy.ticketPolicyId ? ' selected' : ''}`}
                            onClick={() => { setSelectedId(policy.ticketPolicyId); setQuantity(1); }}
                        >
                            <div>
                                <div className="policy-name">{policy.name}</div>
                                <div className="policy-remaining">현재 보유: {getRemaining(policy.ticketPolicyId)}개</div>
                            </div>
                            <span className="policy-price">{policy.price.toLocaleString()}원</span>
                        </div>
                    ))}

                    {selectedId && (
                        <div className="quantity-selector">
                            <span>수량</span>
                            <button className="qty-btn" onClick={() => setQuantity(q => Math.max(1, q - 1))}>-</button>
                            <span className="qty-value">{quantity}</span>
                            <button className="qty-btn" onClick={() => setQuantity(q => q + 1)}>+</button>
                        </div>
                    )}

                    <button
                        className="purchase-btn"
                        onClick={handlePurchase}
                        disabled={!selectedId || loading}
                    >
                        {loading ? '처리 중...' : '구매하기'}
                    </button>
                </div>
        </div>
    );
}
