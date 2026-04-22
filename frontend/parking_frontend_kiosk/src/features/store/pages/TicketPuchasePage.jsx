import { useNavigate } from "react-router-dom";
import useVehicleStore from "../../../store/useVehicleStore";
import useStoreStore from "../../../store/useStoreStore";
import { useEffect, useState } from "react";
import { getTicketPolicies, getWallets, purchaseReady } from "../api/StoreApi";


export default function TicketPurchasePage(){
    const navigate = useNavigate();
    const { setPaymentInfo } = useVehicleStore();
    const { wallets, setWallets } = useStoreStore();
    const [policies, setPolicies] = useState([]);
    const [selectedId, setSelectedId] = useState(null);
    const [quantity, setQuantity] = useState(1);
    const [loading, setLoading] = useState(false);

    useEffect(() =>{
        const load = async ()=>{ 
            const [pList, wList] = await Promise.all([getTicketPolicies(), getWallets()]);
            setPolicies(pList);
            setWallets(wList);
       };
       load();
    },[])

    // 해당 정책의 현재 보유 수량
    const getRemaining = (id) => {
        const w = wallets.find(w => w.ticketPolicyId === id);
        return w ? w.remainingCount : 0;
    };

    const handlePurchase = async ()=>{
        if(!selectedId || loading) return;
        setLoading(true);
        try{
            const res = await purchaseReady(selectedId);
            // STORE_TICKET 플로우 표시
            localStorage.setItem('paymentFlow','STORE_TICKET');
            localStorage.setItem('pendingTicketPolicyId', String(selectedId));
            localStorage.setItem('pendingQuantity', String(quantity));
            // 기존 PaymentPage에 필요한 정보 세팅
            setPaymentInfo({
                orderId: res.orderId,
                orderName: res.orderName,
                amount: res.amount,
                userEmail: null,
                vehicleNumber: '할인권 구매',
            });
            navigate('/payment');
        }catch{
            alert('구매 요청 중 오류가 발생했습니다.');
        }finally{
            setLoading(false);
        }
    };
     return (
      <div style={{ padding: '40px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h2>할인권 구매</h2>
          <button onClick={() => navigate('/store/main')}>돌아가기</button>
        </div>

        <div style={{ marginTop: '24px' }}>
          <h3>할인권 선택</h3>
          {policies.map(policy => (
            <div
              key={policy.ticketPolicyId}
              onClick={() => setSelectedId(policy.ticketPolicyId)}
              style={{
                padding: '16px', margin: '8px 0', cursor: 'pointer',
                border: `2px solid ${selectedId === policy.ticketPolicyId ? '#000' : '#ccc'}`,
                display: 'flex', justifyContent: 'space-between', alignItems: 'center',
              }}
            >
              <div>
                <div>{policy.name}</div>
                <div style={{ fontSize: '12px', color: '#888' }}>
                  현재 보유: {getRemaining(policy.ticketPolicyId)}개
                </div>
              </div>
              <span>{policy.price.toLocaleString()}원</span>
            </div>
          ))}
          
           {selectedId && (                                                                                                      
                <div style={{ display: 'flex', alignItems: 'center', gap: '16px', margin: '16px 0' }}>
                    <span>수량</span>
                    <button onClick={() => setQuantity(q => Math.max(1, q - 1))}>-</button>
                    <span>{quantity}</span>
                    <button onClick={() => setQuantity(q => q + 1)}>+</button>
                </div>
            )}

          <button
            onClick={handlePurchase}
            disabled={!selectedId || loading}
            style={{
              width: '100%', padding: '16px', marginTop: '16px',
              background: selectedId ? '#000' : '#ccc', color: '#fff',
            }}
          >
            {loading ? '처리 중...' : '구매하기'}
          </button>
        </div>
      </div>
    );
}