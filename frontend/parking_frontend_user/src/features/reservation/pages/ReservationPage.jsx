import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
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
    const navigate = useNavigate();

    const memberStatus = localStorage.getItem('userStatus') ?? 'NONE';
    const isResident = memberStatus === 'RESIDENT';

    const handleRestrictedClose = () => navigate(-1);

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