import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Keypad from "../../../shared/components/keypad/Keypad";
import { findCar } from "../api/FindCarApi";
import './FindCarPage.css'
import '../../../shared/styles/prepayment.css'

export default function FindCarPage() {
    const navigate = useNavigate();
    const [query, setQuery] = useState('');
    const [step, setStep] = useState('search');
    const [cars, setCars] = useState([]);
    const [selectedCar, setSelectedCar] = useState(null);

    const handleKey = (k) => setQuery(prev => prev.length < 4 ? prev + String(k) : prev);
    const handleDelete = () => setQuery(prev => prev.slice(0, -1));
    const handleClear = () => setQuery('');

    const handleSearch = async () => {
        if (query.length < 4) return;
        try {
            const result = await findCar(query);
            setCars(result);
            setSelectedCar(result.length > 0 ? result[0] : null);
            setStep('result');
        } catch {
            setCars([]);
            setStep('result');
        }
    };

    if (step === 'result') return (
        <div className="findcar-result-wrapper">
                <div className="findcar-result-header">
                    <h2>내차 찾기</h2>
                    <button className="header-back-button" onClick={() => { setQuery(''); setStep('search'); }}>돌아가기</button>
                </div>

                <div className="findcar-result-body">
                    <div className="findcar-car-list">
                        <h3>검색된 차량</h3>
                        {cars.length === 0 && <p style={{ color: 'var(--muted)', fontWeight: 600 }}>검색 결과가 없습니다.</p>}
                        {cars.map(car => (
                            <div
                                key={car.parkingLogId}
                                className={`findcar-car-item${selectedCar?.parkingLogId === car.parkingLogId ? ' selected' : ''}`}
                                onClick={() => setSelectedCar(car)}
                            >
                                {car.carNumber}
                            </div>
                        ))}
                    </div>

                    {selectedCar && (
                        <div className="findcar-car-detail">
                            <h4>차량 정보</h4>
                            <div className="findcar-car-number">{selectedCar.carNumber}</div>
                            <div className="findcar-space-code">{selectedCar.spaceCode}</div>
                            <div className="findcar-entry-time">
                                입차: {new Date(selectedCar.enteredAt).toLocaleString('ko-KR')}
                            </div>
                        </div>
                    )}
                </div>

                <button className="findcar-home-btn" onClick={() => navigate('/')}>홈으로 돌아가기</button>
        </div>
    );

    return (
        <div className="kiosk-page-wrapper">
                <div className="kiosk-page-header">
                    <h2 className="page-title">내 차 찾기</h2>
                    <button className="header-back-button" onClick={() => navigate('/')}>돌아가기</button>
                </div>
                <p style={{ marginBottom: '8px', letterSpacing: '0.12em', fontSize: '13px', fontWeight: 700, color: 'var(--muted)', textTransform: 'uppercase' }}>
                    차량 번호 끝 4자리
                </p>
                <div className="number-display">{query || '- - - -'}</div>
                <Keypad onKeyClick={handleKey} onDeleteClick={handleDelete} onClearClick={handleClear} />
                <button className="ok-button" onClick={handleSearch} disabled={query.length < 4}>
                    검색
                </button>
        </div>
    );
}
