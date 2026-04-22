import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useMySubscriptions } from '../hooks/useSubscription';
import { vehicleApi } from '../../../features/vehicle/api/vehicleApi';
import SubscriptionCard from '../components/SubscriptionCard';
import SubscriptionBuyCard from '../components/SubscriptionBuyCard';
import SubscriptionHistoryCard from '../components/SubscriptionHistoryCard';
import SubscriptionPurchaseModal from '../components/SubscriptionPurchaseModal';
import './SubscriptionPage.css';

const ResidentRestrictedModal = ({ onClose }) => (
    <div className="resident-only-overlay">
        <div className="resident-only-modal">
            <div className="resident-only-modal__icon">🅿️</div>
            <h3 className="resident-only-modal__title">입주민 전용 주차 구역 운영</h3>
            <p className="resident-only-modal__desc">
                정기권은 방문 차량 전용 서비스입니다.<br />
                입주민 회원께는 전용 주차 구역이<br />별도 제공됩니다.
            </p>
            <button className="resident-only-modal__btn" onClick={onClose}>
                확인
            </button>
        </div>
    </div>
);

export default function SubscriptionPage() {
    const navigate = useNavigate();
    const memberStatus = localStorage.getItem('userStatus') ?? 'NONE';
    const isResident = memberStatus === 'RESIDENT';

    const { data: rawSubscriptions, isLoading } = useMySubscriptions();
    const subscriptions = Array.isArray(rawSubscriptions) ? rawSubscriptions : [];
    const { data: vehicle } = useQuery({
        queryKey: ['myVehicle'],
        queryFn: vehicleApi.getMyVehicle,
    });

    const [modalOpen, setModalOpen] = useState(false);

    if (isResident) {
        return (
            <div className="sub-page">
                <h2 className="sub-page__title">정기권</h2>
                <ResidentRestrictedModal onClose={() => navigate(-1)} />
            </div>
        );
    }

    const now = new Date();
    const activeSubscriptions = subscriptions.filter(s => s.status === 'ACTIVE');

    // 현재 사용 중인 것(now가 기간 안)을 우선, 없으면 가장 빨리 시작하는 미래 정기권
    const currentSubscription = activeSubscriptions.find(
        s => new Date(s.startDate) <= now && now <= new Date(s.endDate)
    );
    const upcomingSubscription = activeSubscriptions
        .filter(s => new Date(s.startDate) > now)
        .sort((a, b) => new Date(a.startDate) - new Date(b.startDate))[0];
    const activeSubscription = currentSubscription ?? upcomingSubscription ?? null;

    // 전체 구매 이력 (카드에 표시 중인 것 포함)
    const history = subscriptions;
    const hasActiveVehicle = vehicle?.status === 'ACTIVE';

    if (isLoading) return <div className="mypage-loading">데이터를 불러오는 중...</div>;

    return (
        <div className="sub-page">
            <h2 className="sub-page__title">정기권</h2>

            <div className="sub-page__grid">
                <SubscriptionCard subscription={activeSubscription} />

                <SubscriptionBuyCard
                    onClick={() => {
                        if (!hasActiveVehicle) return alert('활성화된 차량이 없습니다. 차량을 먼저 등록해주세요.');
                        setModalOpen(true);
                    }}
                />

                {history.length > 0 && (
                    <div className="sub-page__full-width">
                        <SubscriptionHistoryCard history={history} />
                    </div>
                )}
            </div>

            {modalOpen && (
                <SubscriptionPurchaseModal
                    vehicle={vehicle}
                    activeSubscriptions={activeSubscriptions}
                    onClose={() => setModalOpen(false)}
                />
            )}
        </div>
    );
}
