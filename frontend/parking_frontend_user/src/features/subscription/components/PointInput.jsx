import { useState } from 'react';
import Swal from 'sweetalert2';

export default function PointInput({ myPoint, price, onApply }) {
    const [input, setInput] = useState('');

    const handleApply = () => {
        const val = parseInt(input, 10);
        if (isNaN(val) || val < 100) {
            Swal.fire({ icon: 'warning', title: '포인트 사용 불가', text: '최소 100포인트부터 사용 가능합니다.', confirmButtonText: '확인', confirmButtonColor: '#3085d6' });
            return;
        }
        if (val > myPoint) {
            Swal.fire({ icon: 'warning', title: '포인트 부족', text: '보유 포인트가 부족합니다.', confirmButtonText: '확인', confirmButtonColor: '#3085d6' });
            return;
        }
        if (val > price) {
            Swal.fire({ icon: 'warning', title: '한도 초과', text: '정기권 가격을 초과할 수 없습니다.', confirmButtonText: '확인', confirmButtonColor: '#3085d6' });
            return;
        }
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
