import React from 'react'
import './ParkingLogDetailModal.css'
import { PARKING_STATUS_LABELS, PARKING_TYPE_LABELS, PAYMENT_STATUS_LABELS } from '../../../shared/constants/parkingLabel'

const ParkingLogDetailModal = ({ isOpen, data, onClose }) => {
    // 모달이 닫혀있거나 데이터가 없으면 아무것도 렌더링하지 않음
    if (!isOpen || !data) return null;

    return (
        <div className='parking-modal-overlay' onClick={onClose}>
            <div className='parking-modal-content' onClick={(e) => e.stopPropagation()}>

                {/* 헤더 섹션 */}
                <header className='parking-modal-header'>
                    <div className='header-left'>
                        <span className={`type-badge ${data.userType}`}>
                            {PARKING_TYPE_LABELS[data.userType] || data.userType}
                        </span>
                        <h2>
                            {data.carNumber} 상세 정보
                            {data.isBlacklist && <span className='blacklist-tag'>⚠️ BLACKLIST</span>}
                        </h2>
                    </div>
                    <button className='close-x-btn' onClick={onClose}>&times;</button>
                </header>

                <div className='parking-modal-body'>
                    {/* 좌측: 차량 이미지 섹션 */}
                    <div className='modal-left-section'>
                        <div className='image-container'>
                            <p className='image-label'>입차 촬영 기록</p>
                            <div className='main-img-box'>
                                {data.entryPlateImage ? (
                                    <img src={data.entryPlateImage} alt='입차 이미지' />
                                ) : (
                                    <div className='no-image'>이미지가 없습니다.</div>
                                )}
                            </div>
                        </div>
                        {/* 출차 이미지가 있다면 추가로 보여줌 */}
                        {data.exitPlateImage && (
                            <div className='image-container secondary'>
                                <p className='image-label'>출차 촬영 기록</p>
                                <img src={data.exitPlateImage} alt="출차 이미지" className='sub-img' />
                            </div>
                        )}
                    </div>

                    {/* 우측: 상세 정보 섹션 */}
                    <div className='modal-right-section'>
                        {/* 1. 기본/상태 정보 */}
                        <section className='info-group'>
                            <div className='info-row'>
                                <label>현재 상태</label>
                                <div className='value-with-btn'>
                                    <span className={`status-val ${data.parkingStatus}`}>
                                        {PARKING_STATUS_LABELS[data.parkingStatus]}
                                    </span>
                                    {/* 수정 기능은 추후 구현예정 */}
                                    <button className='action-btn-small disabled'>상태변경</button>
                                </div>
                            </div>
                            <div className='info-row'>
                                <label>주차 위치</label>
                                <span>{data.floor}층 / {data.spaceCode}</span>
                            </div>
                            <div className='info-row'>
                                <label>주차 이용시간</label>
                                <span className='duration-text'>{data.parkingDuration}</span>
                            </div>
                        </section>

                        {/* 2. 시간 정보 */}
                        <section className='info-group'>
                            <div className='info-row'>
                                {/* 상태가 입차취소(ENTRY_CANCELLED)일 때만 라벨 변경 */}
                                <label>
                                    {data.parkingStatus === 'ENTRY_CANCELLED' ? '입차 시도' : '입차 시간'}
                                </label>
                                <span>
                                    {data.enteredAt || data.entryTime}
                                    {data.parkingStatus === 'ENTRY_CANCELLED' && <span className='cancel-text'>(취소)</span>}
                                </span>
                            </div>
                            {/* 출차완료(EXITED) 상태이거나 출차시간 데이터가 있을때만 렌더링 */}
                            {(data.parkingStatus === 'EXITED' || data.exitedAt) && (
                                <div className='info-row'>
                                    <label>출차 시간</label>
                                    <span>{data.exitedAt}</span>
                                </div>
                            )}
                            <div className='info-row'>
                                <label>결제 요청</label>
                                <span>{data.paymentRequestedAt || '-'}</span>
                            </div>
                            {/* 입차완료, 아직 출차 하지 않은 '주차중'일 때만 데드라인 표시(출차완료시 불필요) */}
                            {data.parkingStatus !== 'EXITED' && data.parkingStatus !== 'ENTRY_CANCELLED' && (
                                <div className='info-row'>
                                    <label>출차 데드라인</label>
                                    <span className='deadline-text'>{data.freeExitUntil || '-'}</span>
                                </div>
                            )}
                        </section>

                        {/* 3. 결제 정보 */}
                        <section className='info-group payment-info'>
                            <div className='info-row'>
                                <label>원래 요금 (원금)</label>
                                <span>{data.rawFee?.toLocaleString() || 0}원</span>
                            </div>
                            <div className='info-row'>
                                <label>총 할인 금액</label>
                                <div className='value-with-btn'>
                                    <span className='discount-val'>
                                        {data.totalDiscountAmount > 0? `-${data.totalDiscountAmount?.toLocaleString()}원` : '0원'}
                                    </span>
                                    {/* 입차취소나 출차완료나 강제출차가 아닐때만 할인 수정 가능하도록 처리 */}
                                    <button className={`action-btn-small ${['EXITED','ENTRY_CALCELLED','FORCE_EXITED'].includes(data.parkingStatus) ? 'disabled' : ''}`}>
                                        할인수정
                                    </button>
                                </div>
                            </div>
                            <div className='info-row highlight-row'>
                                <label>최종 청구 금액</label>
                                <span className='final-price'>
                                    {(data.calculatedFee || 0).toLocaleString()}원
                                </span>
                            </div>
                            <div className='info-row'>
                                <label>실 결제 금액</label>
                                <span className='paid-amount' style={{fontWeight:'bold', color:data.paymentStatus === 'PAID' ? '#4ade80' : '#ffb0b0'}}>
                                    {data.paymentStatus === 'PAID'? `${data.fee?.toLocaleString()}원` : '0원 (미결제)'}
                                </span>
                            </div>
                            <div className='info-row'>
                                <label>결제 상태</label>
                                <div className='value-with-btn'>
                                    <span className={`payment-val ${data.paymentStatus}`}>
                                        {PAYMENT_STATUS_LABELS[data.paymentStatus] || data.paymentStatus}
                                    </span>
                                    {/* 미납(UNPAID), 차량이 주차장 안에 있는 상태일때만 결제처리 버튼 활성화 */}
                                    <button className={`action-btn-primary ${data.paymentStatus !== 'UNPAID' ? 'disabled' : ''}`}>
                                        결제처리
                                    </button>
                                </div>
                            </div>
                        </section>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default ParkingLogDetailModal