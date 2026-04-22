import { useState } from 'react';

export default function PointInput({ myPoint, price, onApply }) {
    const [input, setInput] = useState('');

    const handleApply = () => {
        const val = parseInt(input, 10);
        if (isNaN(val) || val < 100) return alert('최소 100포인트부터 사용 가능합니다.');
        if (val > myPoint) return alert('보유 포인트가 부족합니다.');
        if (val > price) return alert('정기권 가격을 초과할 수 없습니다.');
        onApply(val);
    };

    const handleUseAll = () => {
        const usable = Math.min(myPoint, price);
        setInput(String(usable));
        onApply(usable);
    };

    const disabled = myPoint < 100;

    return (
        <div className="sub-modal-section">
            <label className="sub-modal-label">
                포인트 사용
                <span className="sub-modal-point-balance"> 보유: {myPoint.toLocaleString()}P</span>
            </label>
            <div className="sub-modal-point-row">
                <input
                    className="sub-modal-input"
                    type="number"
                    placeholder="사용할 포인트 입력"
                    value={input}
                    onChange={e => setInput(e.target.value)}
                    disabled={disabled}
                />
                <button className="sub-modal-btn-outline" onClick={handleApply} disabled={disabled}>적용</button>
                <button className="sub-modal-btn-outline" onClick={handleUseAll} disabled={disabled}>전액</button>
            </div>
        </div>
    );
}
