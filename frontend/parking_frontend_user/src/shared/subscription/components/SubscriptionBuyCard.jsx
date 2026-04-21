import './SubscriptionBuyCard.css';

export default function SubscriptionBuyCard({ onClick }) {
    return (
        <div className="sub-buy-card">
            <div className="sub-buy-card__icon">🎫</div>
            <p className="sub-buy-card__desc">
                정기권 구매 시 주차장을 자유롭게 이용할 수 있습니다.
            </p>
            <button className="sub-buy-card__btn" onClick={onClick}>
                정기권 구매
            </button>
        </div>
    );
}
