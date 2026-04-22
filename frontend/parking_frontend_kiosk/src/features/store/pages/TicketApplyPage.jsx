import Keypad from '../../../shared/components/keypad/Keypad'
import { useNavigate } from "react-router-dom";
import useStoreStore from "../../../store/useStoreStore";
import { useEffect, useState } from "react";
import { applyTicket, getWallets, searchStoreCar } from "../api/StoreApi";

export default function TicketApplyPage(){
    const navigate = useNavigate();
    const { wallets, setWallets } = useStoreStore();
    const [ query, setQuery ] = useState('');
    const [ step, setStep ] = useState('search');
    const [ cars, setCars ] = useState([]);
    const [ selectedCar, setSelectedCar ] = useState(null);
    const [ selectedWallet, setSelectedWallet] = useState(null);
    const [ quantity, setQuantity ] = useState(1);
    const [ loading, setLoading ] = useState(false);

    //remaining > 0 인 지갑만 사용 가능
    const usableWallets = wallets.filter(w => w.remainingCount>0);

    useEffect(()=>{
        const load = async () => {
            const ws = await getWallets();
            setWallets(ws);
        };
        load();
    },[]);
    
    const handleKey = (k) => setQuery(prev => prev.length<4 ? prev + String(k) : prev);
    const handleDelete = () => setQuery(prev => prev.slice(0,-1));
    const handleClear = () => setQuery('');

    const handleSearch = async () => {
        if (query.length < 4) return;
        try {
            const result = await searchStoreCar(query);
            if(!result || result.length === 0) setStep('noResult');
            else{ setCars(result); setStep('carList'); }
        }catch{
            setStep('noResult');
        }
    };

    const handleApply = async () =>{
        if (!selectedCar || !selectedWallet || loading ) return;
        setLoading(true);
        try{
            await applyTicket(selectedCar.parkingLogId, selectedWallet.ticketPolicyId, quantity);
            setStep('done');
        }catch{
            alert(' 할인권 적용 중 오류가 발생했습니다.');
        }finally{
            setLoading(false);
        }
    };

    // 완료
    if (step === 'done') return (
         <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100vh' }}>
        <div style={{ fontSize: '60px' }}>✓</div>
        <h2>할인권 적용이 완료되었습니다</h2>
        <button style={{ marginTop: '32px', padding: '16px 40px' }} onClick={() => navigate('/store/main')}>
          홈으로 돌아가기
        </button>
      </div>
    )
    // 결과 없음
    if (step === 'noResult') return (
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100vh' }}>
        <h3>검색 결과</h3>
        <p>검색 결과가 없습니다</p>
        <button onClick={() => { setQuery(''); setStep('search'); }}>돌아가기</button>
      </div>
    );
    //차량 목록
     if (step === 'carList') return (
      <div style={{ padding: '40px' }}>
        <h3>검색 결과</h3>
        {cars.map(car => (
          <div
            key={car.parkingLogId}
            onClick={() => { setSelectedCar(car); setStep('ticketSelect'); }}
            style={{ padding: '16px', margin: '8px 0', border: '1px solid #ccc', cursor: 'pointer' }}
          >
            <div>{car.carNumber}</div>
            <div style={{ fontSize: '12px', color: '#888' }}>
              입차: {car.entryTime} | 금액: {car.calculatedFee?.toLocaleString()}원
            </div>
          </div>
        ))}
        <button style={{ marginTop: '16px' }} onClick={() => setStep('search')}>닫기</button>
      </div>
    );
    // 할인권 선택
   if (step === 'ticketSelect') return (
      <div style={{ padding: '40px' }}>
        <h3>적용할 할인권 선택</h3>
        <p style={{ color: '#888' }}>선택 차량: {selectedCar?.carNumber}</p>
        {usableWallets.length === 0 && <p style={{ color: 'red' }}>사용 가능한 할인권이 없습니다.</p>}
        {usableWallets.map(w => (
          <div
            key={w.ticketPolicyId}
            onClick={() => { setSelectedWallet(w); setQuantity(1)}}
            style={{
              padding: '16px', margin: '8px 0', cursor: 'pointer',
              border: `2px solid ${selectedWallet?.ticketPolicyId === w.ticketPolicyId ? '#000' : '#ccc'}`,
            }}
          >
            <div>{w.policyName}</div>
            <div style={{ fontSize: '12px', color: '#888' }}>남은 수량: {w.remainingCount}개</div>
          </div>
        ))}
        {selectedWallet && (
          <div style={{ display: 'flex', alignItems: 'center', gap: '16px', margin: '16px 0' }}>
            <span>수량</span>
            <button
              onClick={() => setQuantity(q => Math.max(1, q - 1))}
              style={{ width: '36px', height: '36px', fontSize: '20px' }}
            >-</button>
            <input
              type="number"
              min={1}
              max={selectedWallet.remainingCount}
              value={quantity}
              onChange={e => {
                const v = Number(e.target.value);
                if (v >= 1 && v <= selectedWallet.remainingCount) setQuantity(v);
              }}
              style={{ width: '60px', textAlign: 'center', fontSize: '20px' }}
            />
            <button
              onClick={() => setQuantity(q => Math.min(selectedWallet.remainingCount, q + 1))}
              style={{ width: '36px', height: '36px', fontSize: '20px' }}
            >+</button>
            <span style={{ fontSize: '12px', color: '#888' }}>최대 {selectedWallet.remainingCount}개</span>
          </div>
        )}
        <div style={{ display: 'flex', gap: '16px', marginTop: '16px' }}>
          <button onClick={() => setStep('carList')}>닫기</button>
          <button
            onClick={handleApply}
            disabled={!selectedWallet || loading}
            style={{ background: '#000', color: '#fff', padding: '12px 24px' }}
          >
            {loading ? '처리 중...' : '할인 적용'}
          </button>
        </div>
      </div>
    );
    // 기본: 검색 화면
    return (
      <div style={{ padding: '40px' }}>
        <h2>할인권 적용</h2>
        <p>차량번호 4자리를 입력하세요</p>
        <div style={{ fontSize: '32px', textAlign: 'center', margin: '16px 0' }}>
          {query || '_ _ _ _'}
        </div>
        <Keypad onKeyClick={handleKey} onDeleteClick={handleDelete} onClearClick={handleClear} />
        <button
          style={{
            width: '100%', marginTop: '16px', padding: '14px',
            background: query.length === 4 ? '#000' : '#ccc', color: '#fff',
          }}
          onClick={handleSearch}
          disabled={query.length < 4}
        >
          검색
        </button>
      </div>
    );
}