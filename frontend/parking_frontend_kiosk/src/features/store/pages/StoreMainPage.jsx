import { useNavigate } from "react-router-dom";
import useStoreStore from "../../../store/useStoreStore";
import { useEffect } from "react";
import { getStoreMe, getWallets } from "../api/StoreApi";


export default function StoreMainPage() {
    const navigate = useNavigate();
    const { storeName, wallets, setStoreInfo, setWallets, clearStore } = useStoreStore();
    const totalRemaining = wallets.reduce((sum,w) => sum + w.remainingCount, 0);

    useEffect(()=>{
        const load = async () =>{
            try {
                const me = await getStoreMe();
                setStoreInfo(me.storeId, me.storeName, localStorage.getItem('storeToken'));
                const ws = await getWallets();
                setWallets(ws);
            }catch{
                navigate('/store/login');
            }
        };
        load();
    },[]);

    const handleLogout = ()=>{
        localStorage.removeItem('storeToken');
        clearStore();
        navigate('/store/login');
    }
    return (
      <div style={{ padding: '40px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
          <div>
            <h2>{storeName}</h2>
            <p>남은 주차권: {totalRemaining}개</p>
          </div>
          <button onClick={handleLogout}>로그아웃</button>
        </div>

        <div style={{ display: 'flex', gap: '40px', marginTop: '40px' }}>
          <div style={{ flex: 1 }}>
            <h3>할인권 적용</h3>
            <button
              style={{ width: '100%', padding: '16px', background: '#000', color: '#fff' }}
              onClick={() => navigate('/store/apply')}
            >
              차량 번호 검색
            </button>
          </div>

          <div style={{ border: '1px solid #ccc', padding: '20px', minWidth: '200px' }}>
            <p><strong>상가명</strong><br />{storeName}</p>
            <p><strong>남은 주차권</strong><br />{totalRemaining}개</p>
            <button
              style={{ width: '100%', padding: '12px', marginTop: '12px', background: '#000', color: '#fff' }}
              onClick={() => navigate('/store/purchase')}
            >
              할인권 구매
            </button>
          </div>
        </div>
      </div>
    );
}