import React, { useState } from 'react'; // 1. useState 추가
import ReservationList from '../components/ReservationList';
import ReservationModal from '../components/ReservationModal'; // 2. 모달 컴포넌트 임포트
import './ReservationPage.css';

const ReservationPage = () => {
    // 3. 모달의 열림 상태 관리 (기본값: 닫힘)
    const [isModalOpen, setIsModalOpen] = useState(false);

    return (
        <div className="reservation-page">
            <h2 className="reservation-page__title">방문 예약</h2>

            <ReservationList />

            <div className="reservation-page__footer">
                {/* 4. 버튼 클릭 시 모달 열기 */}
                <button
                    className="btn-new-reservation"
                    onClick={() => setIsModalOpen(true)}
                >
                    방문 예약 신청
                </button>
            </div>

            {/* 5. 모달 배치: 상태값과 닫기 함수를 전달합니다 */}
            <ReservationModal 
                isOpen={isModalOpen} 
                onClose={() => setIsModalOpen(false)} 
            />
        </div>
    );
};

export default ReservationPage;