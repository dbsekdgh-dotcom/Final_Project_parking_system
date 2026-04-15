import React from 'react';
import { useCancelReservation, useMyReservation } from '../hooks/useReservation';
import Swal from 'sweetalert2';
import './ReservationList.css';

const STATUS_CONFIG = {
    PENDING:   { text: '승인대기', cls: 'status-badge--pending' },
    RESERVED:  { text: '예약완료', cls: 'status-badge--reserved' },
    REJECTED:  { text: '거절됨',   cls: 'status-badge--rejected' },
    ENTERED:   { text: '주차중',   cls: 'status-badge--entered' },
    CANCELLED: { text: '취소됨',   cls: 'status-badge--cancelled' },
    COMPLETED: { text: '이용완료', cls: 'status-badge--completed' },
};

const PURPOSE_MAP = {
    FAMILY:   '가족 방문',
    FRIEND:   '지인 방문',
    BUSINESS: '업무/배달',
    DELIVERY: '배달/택배',
    OTHER:    '기타',
};

const ReservationList = () => {
    const { data: reservations, isLoading, isError } = useMyReservation();
    const { mutate: cancel } = useCancelReservation();

    const formatDateTime = (dateStr) => {
        if (!dateStr) return '-';
        return new Date(dateStr).toLocaleString('ko-KR', {
            month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit',
        });
    };

    const handleCancel = (id) => {
        Swal.fire({
            title: '예약 취소',
            text: '정말로 취소하시겠습니까?',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#d33',
            cancelButtonColor: '#6c757d',
            confirmButtonText: '확인',
            cancelButtonText: '취소',
        }).then((result) => {
            if (result.isConfirmed) cancel(id);
        });
    };

    if (isLoading) return <div className="reservation-loading">데이터를 불러오는 중...</div>;
    if (isError)   return <div className="reservation-error">데이터 로드에 실패했습니다.</div>;

    return (
        <div className="reservation-card">
            <h3 className="reservation-card__title">방문 예약 내역</h3>

            <div className="reservation-table-wrap">
                <table className="reservation-table">
                    <thead>
                        <tr>
                            <th className="center">차량번호</th>
                            <th className="left">방문 목적 및 시간</th>
                            <th className="center">상태</th>
                            <th className="center">관리</th>
                        </tr>
                    </thead>
                    <tbody>
                        {reservations && reservations.length > 0 ? (
                            reservations.map((res) => {
                                const status = STATUS_CONFIG[res.status] ?? { text: res.status, cls: 'status-badge--cancelled' };
                                return (
                                    <tr key={res.reservationId}>
                                        <td className="center">
                                            <span className="res-car-number">{res.carNumber}</span>
                                        </td>
                                        <td className="left">
                                            <div className="res-purpose">
                                                {PURPOSE_MAP[res.purpose] || '기타 방문'}
                                            </div>
                                            <div className="res-time">
                                                {formatDateTime(res.visitStartAt)} ~ {formatDateTime(res.visitEndAt)}
                                            </div>
                                        </td>
                                        <td className="center">
                                            <span className={`status-badge ${status.cls}`}>
                                                {status.text}
                                            </span>
                                        </td>
                                        <td className="center">
                                            {['PENDING', 'RESERVED'].includes(res.status) && (
                                                <button
                                                    className="btn-cancel-res"
                                                    onClick={() => handleCancel(res.reservationId)}
                                                >
                                                    취소요청
                                                </button>
                                            )}
                                        </td>
                                    </tr>
                                );
                            })
                        ) : (
                            <tr>
                                <td colSpan="4" className="reservation-empty">
                                    예약된 내역이 없습니다.
                                </td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default ReservationList;
