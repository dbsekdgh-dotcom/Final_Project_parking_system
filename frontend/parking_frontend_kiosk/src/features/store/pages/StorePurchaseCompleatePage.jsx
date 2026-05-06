import { useNavigate } from "react-router-dom";
import './StorePurchaseCompleatePage.css';

export default function StorePurchaseCompletePage() {
    const navigate = useNavigate();
    return (
        <div className="purchase-complete-wrapper">
            <div className="purchase-complete-card">
                <div className="purchase-complete-icon">✓</div>
                <h2>할인권 구매가 완료되었습니다.</h2>
                <p>구매하신 할인권이 지갑에 추가되었습니다.</p>
                <button className="btn-black" onClick={() => navigate('/store/main')}>
                    상가 홈으로
                </button>
            </div>
        </div>
    );
}
