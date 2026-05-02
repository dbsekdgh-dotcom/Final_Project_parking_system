import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import Swal from 'sweetalert2';
import { useCancelVehicle, useDeleteVehicle, useMyVehicle } from '../hooks/useVehicle';
import VehicleRegisterModal from './VehicleRegisterModal';
import './VehicleCard.css';

const VehicleCard = () => {
    const { data: vehicle, isLoading } = useMyVehicle();
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [searchParams, setSearchParams] = useSearchParams();

    useEffect(() => {
        if (searchParams.get('openVehicle') === '1') {
            setSearchParams({}, { replace: true });
            setIsModalOpen(true);
        }
    }, [searchParams]);

    const { mutate: cancelVehicle } = useCancelVehicle();
    const { mutate: deleteVehicle } = useDeleteVehicle();

    const handleDeleteVehicle = async (vehicleId) => {
        const result = await Swal.fire({
            title: '차량을 삭제하시겠습니까?',
            text: '삭제된 차량은 복구할 수 없습니다.',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#d33',
            cancelButtonColor: '#6c757d',
            confirmButtonText: '삭제',
            cancelButtonText: '취소',
        });
        if (result.isConfirmed) {
            deleteVehicle(vehicleId);
        }
    };

    if (isLoading) return <div className="vehicle-card"><p className="vehicle-card__model">로딩 중...</p></div>;

    return (
        <div className="vehicle-card">
            <h3 className="vehicle-card__title">차량 관리</h3>

            {!vehicle ? (
                <div className="vehicle-card__empty">
                    <p>등록된 차량이 없습니다.</p>
                    <button className="btn-vehicle btn-vehicle--blue" onClick={() => setIsModalOpen(true)}>
                        차량 등록
                    </button>
                </div>
            ) : (
                <>
                    <div className="vehicle-card__info">
                        <p className="vehicle-card__number">{vehicle.carNumber}</p>
                        {vehicle.vehicleName && (
                            <p className="vehicle-card__model">{vehicle.vehicleName}</p>
                        )}
                    </div>
                    <div className="vehicle-card__footer">
                        {vehicle.status === 'PENDING' ? (
                            <>
                                <span className="vehicle-badge vehicle-badge--pending">승인 대기 중</span>
                                <button
                                    className="btn-vehicle btn-vehicle--gray"
                                    onClick={() => cancelVehicle({ vehicleId: vehicle.vehicleId, approvalId: vehicle.approvalId })}
                                >
                                    신청 취소
                                </button>
                            </>
                        ) : (
                            <>
                                <span className="vehicle-badge vehicle-badge--active">등록 완료</span>
                                <button
                                    className="btn-vehicle btn-vehicle--red"
                                    onClick={() => handleDeleteVehicle(vehicle.vehicleId)}
                                >
                                    차량 삭제
                                </button>
                            </>
                        )}
                    </div>
                </>
            )}

            {isModalOpen && (
                <VehicleRegisterModal
                    isOpen={isModalOpen}
                    onClose={() => setIsModalOpen(false)}
                />
            )}
        </div>
    );
};

export default VehicleCard;
