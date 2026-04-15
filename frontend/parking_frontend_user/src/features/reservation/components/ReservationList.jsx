import React, { useState } from 'react';
import { useCancelReservation, useMyReservation } from '../hooks/useReservation';
import Swal from 'sweetalert2';
import './ReservationList.css';
import ReservationSummary from './ReservationSummary';
import ReservationActionButtons from './ReservationActionButtons';
import ReservationEditModal from './ReservationEditModal';

const STATUS_CONFIG = {
    PENDING:   { text: '승인대기',   cls: 'status-badge--pending' },
    RESERVED:  { text: '예약완료',   cls: 'status-badge--reserved' },
    REJECTED:  { text: '거절됨',     cls: 'status-badge--rejected' },
    ENTERED:   { text: '주차중',     cls: 'status-badge--entered' },
    CANCELLED: { text: '취소됨',     cls: 'status-badge--cancelled' },
    COMPLETED: { text: '이용완료',   cls: 'status-badge--completed' },
    NO_SHOW:   { text: '무단 미방문', cls: 'status-badge--noshow' },
};

const PURPOSE_MAP = {
    FAMILY:   '가족 방문',
    FRIEND:   '지인 방문',
    BUSINESS: '업무',
    DELIVERY: '배달/택배',
    OTHER:    '기타',
};

const PAGE_SIZE = 5;

const ReservationList = () => {
    const { data: reservations, isLoading, isError } = useMyReservation();
    const { mutate: cancel } = useCancelReservation();

    const [currentPage, setCurrentPage]     = useState(1);
    const [editTarget, setEditTarget]       = useState(null);
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);

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

    const handleEditOpen = (reservation) => {
        setEditTarget(reservation);
        setIsEditModalOpen(true);
    };

    const handleEditClose = () => {
        setIsEditModalOpen(false);
        setEditTarget(null);
    };

    if (isLoading) return <div className="reservation-loading">데이터를 불러오는 중...</div>;
    if (isError)   return <div className="reservation-error">데이터 로드에 실패했습니다.</div>;

    const total      = reservations?.length ?? 0;
    const totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE));
    const safePage   = Math.min(currentPage, totalPages);
    const paged      = (reservations ?? []).slice((safePage - 1) * PAGE_SIZE, safePage * PAGE_SIZE);

    const pageGroupStart = Math.floor((safePage - 1) / 5) * 5 + 1;
    const pageGroupEnd   = Math.min(pageGroupStart + 4, totalPages);
    const pageNumbers    = Array.from(
        { length: pageGroupEnd - pageGroupStart + 1 },
        (_, i) => pageGroupStart + i
    );

    return (
        <>
            <div className="reservation-card">
                <ReservationSummary />
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
                            {paged.length > 0 ? (
                                paged.map((res) => {
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
                                                <ReservationActionButtons
                                                    status={res.status}
                                                    onCancel={() => handleCancel(res.reservationId)}
                                                    onEdit={() => handleEditOpen(res)}
                                                />
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

                {totalPages > 1 && (
                    <div className="pagination">
                        <button
                            className="pagination__btn"
                            onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
                            disabled={safePage === 1}
                        >
                            이전
                        </button>
                        {pageNumbers.map((n) => (
                            <button
                                key={n}
                                className={`pagination__btn pagination__num${n === safePage ? ' active' : ''}`}
                                onClick={() => setCurrentPage(n)}
                            >
                                {n}
                            </button>
                        ))}
                        <button
                            className="pagination__btn"
                            onClick={() => setCurrentPage((p) => Math.min(totalPages, p + 1))}
                            disabled={safePage === totalPages}
                        >
                            다음
                        </button>
                    </div>
                )}
            </div>

            {/* 수정 모달 — 리스트에서 하나만 관리 */}
            <ReservationEditModal
                isOpen={isEditModalOpen}
                onClose={handleEditClose}
                reservation={editTarget}
            />
        </>
    );
};

export default ReservationList;
