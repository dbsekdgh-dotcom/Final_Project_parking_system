import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../auth/api/axios';
import ReservationList from '../components/ReservationList';
import ReservationModal from '../components/ReservationModal';
import './ReservationPage.css';

const ResidentOnlyModal = ({ onClose }) => (
    <div className="resident-only-overlay">
        <div className="resident-only-modal">
            <div className="resident-only-modal__icon">🔒</div>
            <h3 className="resident-only-modal__title">입주민 전용 서비스</h3>
            <p className="resident-only-modal__desc">
                방문 예약은 입주민 등록 후<br />이용 가능한 서비스입니다.
            </p>
            <button className="resident-only-modal__btn" onClick={onClose}>
                확인
            </button>
        </div>
    </div>
);

const ReservationPage = () => {
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isResident, setIsResident] = useState(localStorage.getItem('userStatus') === 'RESIDENT');
    const [isLoading, setIsLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        api.get('/api/user/auth/local/me')
            .then(res => {
                const status = res.data.userStatus ?? 'NONE';
                localStorage.setItem('userStatus', status);
                if (res.data.unitNo != null) localStorage.setItem('unitNo', String(res.data.unitNo));
                setIsResident(status === 'RESIDENT');
            })
            .catch(() => {
                setIsResident(localStorage.getItem('userStatus') === 'RESIDENT');
            })
            .finally(() => setIsLoading(false));
    }, []);

    const handleRestrictedClose = () => navigate(-1);

    if (isLoading) return null;

    return (
        <div className="reservation-page">
            <h2 className="reservation-page__title">방문 예약</h2>

            {isResident ? (
                <>
                    <ReservationList />
                    <div className="reservation-page__footer">
                        <button
                            className="btn-new-reservation"
                            onClick={() => setIsModalOpen(true)}
                        >
                            방문 예약 신청
                        </button>
                    </div>
                    <ReservationModal
                        isOpen={isModalOpen}
                        onClose={() => setIsModalOpen(false)}
                    />
                </>
            ) : (
                <ResidentOnlyModal onClose={handleRestrictedClose} />
            )}
        </div>
    );
};

export default ReservationPage;