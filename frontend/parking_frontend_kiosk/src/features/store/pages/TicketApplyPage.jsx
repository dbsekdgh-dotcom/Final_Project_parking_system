import Keypad from '../../../shared/components/keypad/Keypad'
import { useNavigate } from "react-router-dom";
import useStoreStore from "../../../store/useStoreStore";
import { useEffect, useState } from "react";
import { applyTicket, getWallets, searchStoreCar } from "../api/StoreApi";
import './TicketApplyPage.css';

export default function TicketApplyPage() {
    const navigate = useNavigate();
    const { wallets, setWallets } = useStoreStore();
    const [query, setQuery] = useState('');
    const [step, setStep] = useState('search');
    const [cars, setCars] = useState([]);
    const [selectedCar, setSelectedCar] = useState(null);
    const [selectedWallet, setSelectedWallet] = useState(null);
    const [quantity, setQuantity] = useState(1);
    const [loading, setLoading] = useState(false);

    const usableWallets = wallets.filter(w => w.remainingCount > 0);

    useEffect(() => {
        const load = async () => {
            const ws = await getWallets();
            setWallets(ws);
        };
        load();
    }, []);

    const handleKey = (k) => setQuery(prev => prev.length < 4 ? prev + String(k) : prev);
    const handleDelete = () => setQuery(prev => prev.slice(0, -1));
    const handleClear = () => setQuery('');

    const handleSearch = async () => {
        if (query.length < 4) return;
        try {
            const result = await searchStoreCar(query);
            if (!result || result.length === 0) setStep('noResult');
            else { setCars(result); setStep('carList'); }
        } catch {
            setStep('noResult');
        }
    };

    const handleApply = async () => {
        if (!selectedCar || !selectedWallet || loading) return;
        setLoading(true);
        try {
            await applyTicket(selectedCar.parkingLogId, selectedWallet.ticketPolicyId, quantity);
            setStep('done');
        } catch {
            alert('할인권 적용 중 오류가 발생했습니다.');
        } finally {
            setLoading(false);
        }
    };

    if (step === 'done') return (
        <div className="store-page-root">
            <div className="apply-done-wrapper">
                <div className="apply-done-icon">✓</div>
                <h2>할인권 적용이 완료되었습니다</h2>
                <button className="btn-black" onClick={() => navigate('/store/main')}>홈으로 돌아가기</button>
            </div>
        </div>
    );

    if (step === 'noResult') return (
        <div className="store-page-root">
            <div className="apply-no-result">
                <h3>검색 결과</h3>
                <p>검색 결과가 없습니다</p>
                <button className="btn-black" onClick={() => { setQuery(''); setStep('search'); }}>돌아가기</button>
            </div>
        </div>
    );

    if (step === 'carList') return (
        <div className="store-page-root">
            <div className="apply-car-list-wrapper">
                <h3>검색 결과</h3>
                {cars.map(car => (
                    <div
                        key={car.parkingLogId}
                        className="car-item"
                        onClick={() => { setSelectedCar(car); setStep('ticketSelect'); }}
                    >
                        <div className="car-number">{car.carNumber}</div>
                        <div className="car-sub">입차: {car.entryTime} | 금액: {car.calculatedFee?.toLocaleString()}원</div>
                    </div>
                ))}
                <button className="btn-outline" style={{ marginTop: '16px' }} onClick={() => setStep('search')}>닫기</button>
            </div>
        </div>
    );

    if (step === 'ticketSelect') return (
        <div className="store-page-root">
            <div className="apply-ticket-wrapper">
                <h3>적용할 할인권 선택</h3>
                <p className="apply-car-label">선택 차량: {selectedCar?.carNumber}</p>
                {usableWallets.length === 0 && <p className="no-wallet-msg">사용 가능한 할인권이 없습니다.</p>}
                {usableWallets.map(w => (
                    <div
                        key={w.ticketPolicyId}
                        className={`wallet-item${selectedWallet?.ticketPolicyId === w.ticketPolicyId ? ' selected' : ''}`}
                        onClick={() => { setSelectedWallet(w); setQuantity(1); }}
                    >
                        <div className="wallet-name">{w.policyName}</div>
                        <div className="wallet-sub">남은 수량: {w.remainingCount}개</div>
                    </div>
                ))}
                {selectedWallet && (
                    <div className="apply-quantity-selector">
                        <span>수량</span>
                        <button className="apply-qty-btn" onClick={() => setQuantity(q => Math.max(1, q - 1))}>-</button>
                        <input
                            type="number"
                            min={1}
                            max={selectedWallet.remainingCount}
                            value={quantity}
                            onChange={e => {
                                const v = Number(e.target.value);
                                if (v >= 1 && v <= selectedWallet.remainingCount) setQuantity(v);
                            }}
                            className="apply-qty-input"
                        />
                        <button className="apply-qty-btn" onClick={() => setQuantity(q => Math.min(selectedWallet.remainingCount, q + 1))}>+</button>
                        <span className="apply-qty-max">최대 {selectedWallet.remainingCount}개</span>
                    </div>
                )}
                <div className="apply-action-buttons">
                    <button className="btn-outline" onClick={() => setStep('carList')}>닫기</button>
                    <button className="btn-black" onClick={handleApply} disabled={!selectedWallet || loading}>
                        {loading ? '처리 중...' : '할인 적용'}
                    </button>
                </div>
            </div>
        </div>
    );

    return (
        <div className="store-page-root">
            <div className="apply-search-wrapper">
                <div className="apply-search-header">
                    <h2>할인권 적용</h2>
                    <button className="btn-outline" onClick={() => navigate('/store/main')}>뒤로가기</button>
                </div>
                <p>차량번호 4자리를 입력하세요</p>
                <div className="apply-query-display">{query || '- - - -'}</div>
                <Keypad onKeyClick={handleKey} onDeleteClick={handleDelete} onClearClick={handleClear} />
                <button className="apply-search-btn" onClick={handleSearch} disabled={query.length < 4}>
                    검색
                </button>
            </div>
        </div>
    );
}
