import { useNavigate } from "react-router-dom";
import useStoreStore from "../../../store/useStoreStore";
import { useEffect } from "react";
import { getStoreMe, getWallets } from "../api/StoreApi";
import './StoreMainPage.css';

export default function StoreMainPage() {
    const navigate = useNavigate();
    const { storeName, wallets, setStoreInfo, setWallets, clearStore } = useStoreStore();
    const totalRemaining = wallets.reduce((sum, w) => sum + w.remainingCount, 0);

    useEffect(() => {
        const load = async () => {
            try {
                const me = await getStoreMe();
                setStoreInfo(me.storeId, me.storeName, localStorage.getItem('storeToken'));
                const ws = await getWallets();
                setWallets(ws);
            } catch {
                navigate('/store/login');
            }
        };
        load();
    }, []);

    const handleLogout = () => {
        localStorage.removeItem('storeToken');
        clearStore();
        navigate('/store/login');
    };

    return (
        <div className="store-main-wrapper">
            <div className="store-main-header">
                <div>
                    <h2 className="store-main-name">{storeName}</h2>
                    <p className="store-main-remaining">남은 주차권: {totalRemaining}개</p>
                </div>
                <button className="btn-logout" onClick={handleLogout}>로그아웃</button>
            </div>

            <div className="store-main-body">
                <div className="store-apply-section">
                    <h3>할인권 적용</h3>
                    <div className="store-apply-input-display">예: 1234</div>
                    <button className="store-search-btn" onClick={() => navigate('/store/apply')}>검색</button>
                </div>

                <div className="store-info-card">
                    <p className="store-info-label">상가명</p>
                    <p className="store-info-value">{storeName}</p>
                    <p className="store-info-label">남은 주차권</p>
                    <p className="store-info-value">{totalRemaining}개</p>
                    <button className="store-purchase-btn" onClick={() => navigate('/store/purchase')}>할인권 구매</button>
                </div>
            </div>
        </div>
    );
}
