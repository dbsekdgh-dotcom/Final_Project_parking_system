export default function SubscriptionPriceInfo({ price, remaining }) {
    return (
        <div className="sub-modal-section sub-modal-price-box">
            <div className="sub-modal-price-row">
                <span>정기권 가격</span>
                <span>{price?.toLocaleString()}원</span>
            </div>
            <div className="sub-modal-price-row">
                <span>잔여 수량</span>
                <span>{remaining}개</span>
            </div>
        </div>
    );
}
