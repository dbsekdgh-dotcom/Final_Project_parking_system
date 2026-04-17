import React, { useEffect, useMemo, useState } from 'react'
import './ParkingLogDetailModal.css'
import { PARKING_STATUS_LABELS, PARKING_TYPE_LABELS, PAYMENT_STATUS_LABELS } from '../../../shared/constants/parkingLabel'
import { processForceExit, modifyDiscount as modifyDiscountApi, getAdminTicketPolicies } from '../api/parkingLogApi'

const ParkingLogDetailModal = ({ isOpen, data, onClose, onRefresh, onRefetchDetail }) => {
    const [isSubmitting, setIsSubmitting] = useState(false); //버튼을 여러번 클릭시 에러 방지용
    const [isEditingDiscount, setIsEditingDiscount] = useState(false); //할인수정 버튼 클릭 시 실행용
    const [editReason, setEditReason] = useState("");
    const [policies,setPolicies]=useState([])
    const [selectedPolicyId,setSelectedPolicyId] = useState("")

    //모달이 열릴때 관리자용 할인 정책 목록 로드
    useEffect(()=>{
        if(isOpen){
            const fetchPolicies = async()=>{
                try{
                    const res= await getAdminTicketPolicies()
                    setPolicies(res.data || [])
                }catch(err){
                    console.error("할인 정책 로드 실패:",err)
                }
            }
            fetchPolicies();
        } else {
            //모달 닫힐때 초기화
            setIsEditingDiscount(false)
            setSelectedPolicyId("")
            setEditReason("")
        }
    },[isOpen])

    const getCalculatedStatus =useMemo(()=>{
        if(!data) return {label: "데이터 없음", class:"NONE"}
        const {paymentStatus,calculatedFee,parkingStatus}=data;
        //이미 출차했거나 결제 완료된 건 DB 상태 우선
        if (['PAID','SUCCESS'].includes(paymentStatus)) return {label: "결제 완료", class:"PAID"}
        if(parkingStatus === 'EXITED') return {label: "출차 완료", class: "EXITED"}
        //실시간 계산 금액이 0보다 큰경우
        if(calculatedFee > 0 ) return {label: "미납 (결제 필요)", class:"UNPAID"}
        //금액이 0원인 경우
        if(paymentStatus === 'NONE') return {label: "무료 (대상 차량)", class:"NONE"}
        return {label: PAYMENT_STATUS_LABELS[paymentStatus] || paymentStatus, class:paymentStatus}
    },[data]);

    

    // 모달이 닫혀있거나 데이터가 없으면 아무것도 렌더링하지 않음
    if (!isOpen || !data) return null;

    

    // 상태변경 버튼 활성화 조건: 입차 완료 상태(ENTERED) 또는 입차 기록이 있는 경우 중,
    // 이미 출차완료(EXITED), 강제출차(FORCE_EXITED)가 아닌 경우만 활성화
    const isForceExitDisabled = ['EXITED', 'FORCE_EXITED', 'ENTRY_CANCELLED'].includes(data.parkingStatus)
    const isDiscountEditDisabled =
        //이미 출차했거나 취소된 상태일 때
        ['EXITED', 'ENTRY_CANCELLED', 'FORCE_EXITED'].includes(data.parkingStatus) ||
        //결제가 완료되었거나 프로세스가 종료된 상태일 때
        ['SUCCESS', 'FAILED', 'CANCELLED','REFUNDED'].includes(data.paymentStatus) ||
        //입주민(RESIDENT) 또는 정기권(SUBSCRIPTION) 차량인 경우
        ['RESIDENT','SUBSCRIPTION'].includes(data.userType);

    // 강제 출차 핸들러
    const handleForceExitClick = async () => {
        if (isForceExitDisabled) return;

        const reason = window.prompt("강제 출차 사유를 입력해주세요.", "관리자 직접 조치");

        if (reason === null) return; //취소 클릭시
        if (reason.trim() === "") {
            alert("사유를 반드시 입력해야 합니다.");
            return;
        }

        if (window.confirm(`${data.carNumber} 차량을 강제 출차 처리하시겠습니까?`)) {
            try {
                setIsSubmitting(true);
                const res = await processForceExit(data.parkingLogId, reason);
                alert(res.message || "강제 출차 처리가 완료되었습니다.");
                if (onRefresh) { await onRefresh() } //부모 컴포넌트 새로고침 함수 호출
                if(onRefetchDetail) { await onRefetchDetail(data.parkingLogId) }

            } catch (error) {
                alert(error.response?.data?.message || "처리에 실패했습니다.");
            } finally {
                setIsSubmitting(false);
            }
        }
    }

    //할인 수정 시작 버튼 클릭 시 
    const handleStartEdit = () => {
        if (isDiscountEditDisabled) return

        setEditReason("사유: 관리자가 직접 수정");
        setIsEditingDiscount(true);
    }

    //정책 선택 시 자동 사유 입력
    const handlePolicyChange=(e)=>{
        const policyId=e.target.value;
        setSelectedPolicyId(policyId)
        
        if(policyId) {
            //선택한 정책의 이름을 찾아 사유에 기본값으로 넣어주기
            const selectedPolicy = policies.find(p=>String(p.id) === String(policyId));
            if(selectedPolicy){
                setEditReason(`관리자 직권 할인: ${selectedPolicy.name}`)
            }
        } else {
            setEditReason("")
        }
    }

    //할인 수정 저장
    const handleSaveDiscount = async () => {
        if (!selectedPolicyId) return alert("적용할 할인 정책을 선택해주세요.")
        if (!editReason.trim()) return alert("수정 사유를 입력해주세요.")
        try {
            setIsSubmitting(true)
            const res = await modifyDiscountApi(data.parkingLogId, selectedPolicyId, editReason)
            alert(res.message || "할인권이 성공적으로 적용되었습니다.")
            setIsEditingDiscount(false)
            //부모 리스트 및 데이터 새로고침
            if (onRefresh) { await onRefresh() }
            if(onRefetchDetail) { await onRefetchDetail(data.parkingLogId) }

        } catch (error) {
            const errorMsg = error.response?.data?.message || "수정에 실패했습니다."
            alert(errorMsg)
        } finally {
            setIsSubmitting(false)
        }
    }

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
                            <div className='img-box'>
                                {data.entryPlateImage ? (
                                    <img src={data.entryPlateImage} alt='입차 이미지' />
                                ) : (
                                    <div className='no-image'>이미지가 없습니다.</div>
                                )}
                            </div>
                        </div>
                        {/* 출차 이미지가 있다면 추가로 보여줌 */}
                        {data.exitPlateImage && (
                            <div className='image-container'>
                                <p className='image-label'>출차 촬영 기록</p>
                                <div className='img-box'>
                                    <img src={data.exitPlateImage} alt="출차 이미지" />
                                </div>
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
                                    {/* 강제출차 버튼 활성화 / isForceExitDisabled가 true면 disabled 클래스 추가 및 클릭 방지 */}
                                    <button className={`action-btn-small ${isForceExitDisabled || isSubmitting ? 'disabled' : 'danger-btn'}`}
                                        onClick={handleForceExitClick} disabled={isForceExitDisabled || isSubmitting}>
                                        {isSubmitting ? '처리중...' : '강제출차'}
                                    </button>
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
                                <label>결제 시간</label>
                                <span>{data.paidAt || '-'}</span>
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
                                <label>실시간 발생 요금 (원금)</label>
                                <span>{data.rawFee?.toLocaleString() || 0}원</span>
                            </div>
                            {/* 상세 할인 내역 표시(상가/관리자 분리) */}
                            {(data.storeDiscountTotal > 0 || data.adminDiscountTotal > 0 ) && (
                                <div className='discount-detail-rows'>
                                    {data.storeDiscountTotal > 0 && (
                                        <div className='info-row sub-row'>
                                            <label>└ 상가 할인권</label>
                                            <span className='discount-val-sub'>-{data.storeDiscountTotal?.toLocaleString()}원</span>
                                        </div>
                                    )}
                                    {data.adminDiscountTotal > 0 && (
                                        <div className='info-row sub-row'>
                                            <label>└ 관리자 직권 할인</label>
                                            <span className='discount-val-sub'>-{data.adminDiscountTotal?.toLocaleString()}원</span>
                                        </div>
                                    )}
                                </div>
                            )}

                            <div className='info-row'>
                                <label>총 할인 금액 합계</label>
                                <div className='value-with-btn'>
                                    {isEditingDiscount ? (
                                        <div className='discount-edit-form'>
                                            <div className='input-group column'>
                                                <select value={selectedPolicyId} onChange={handlePolicyChange}
                                                    className='edit-input policy-select'>
                                                        <option value="">할인 정책 선택</option>
                                                        {policies.map(p=>(
                                                            <option key={p.id} value={p.id}>
                                                                {p.name} ({p.discountType === 'FREE'?'전액무료' :
                                                                p.discountType === 'TIME' ? `${p.discountValue}분` :
                                                                `${p.discountValue.toLocaleString()}원`})
                                                            </option>
                                                        ))}
                                                </select>
                                                <input type='text' value={editReason} onChange={(e)=>setEditReason(e.target.value)}
                                                placeholder='수정 사유 입력' className='edit-input reason'/>
                                            </div>
                                            <div className='edit-btns'>
                                                <button className='save-btn' onClick={handleSaveDiscount} disabled={isSubmitting}>
                                                    {isSubmitting ? '...' : '저장'}
                                                </button>
                                                <button className='cancel-btn' onClick={() => setIsEditingDiscount(false)}>
                                                    취소
                                                </button>
                                            </div>
                                        </div>
                                    ) : (
                                        <>
                                            <span className='discount-val total-highlight'>
                                                {data.totalDiscountAmount > 0 ? `-${data.totalDiscountAmount?.toLocaleString()}원` : '0원'}
                                            </span>
                                            {/* 입차취소나 출차완료나 강제출차가 아닐때만 할인 수정 가능하도록 처리 */}
                                            <button className={`action-btn-small ${isDiscountEditDisabled ? 'disabled' : ''}`}
                                                onClick={handleStartEdit} disabled={isDiscountEditDisabled || isSubmitting}>
                                                할인수정
                                            </button>
                                        </>
                                    )}
                                </div>
                            </div>
                            <div className='info-row highlight-row'>
                                <label>최종 청구 금액</label>
                                <span className='final-price'>
                                    {data.calculatedFee?.toLocaleString() || 0}원
                                </span>
                            </div>
                            <div className='info-row'>
                                <label>실 결제 금액</label>
                                <span className='paid-amount' style={{ fontWeight: 'bold', color: (data.paymentStatus === 'NONE' || data.paymentStatus === 'PAID') ? '#4ade80' : '#ffb0b0' }}>
                                    {(()=>{
                                        // 무료 대상 차량 (정기권, 입주민, 회차)
                                        if(data.paymentStatus === 'NONE' && data.calculatedFee <= 0) return '무료';
                                        // 결제 금액이 있는 경우 (일부결제)
                                        if(data.fee > 0) {
                                            return `${data.fee.toLocaleString()}원 ${data.paymentStatus !== 'PAID' ? '(일부결제)' : ''}`;
                                        }
                                        if(data.paymentStatus === 'PAID') return `${data.fee.toLocaleString()}원`
                                        // 미결제
                                        return '0원 (미결제)';
                                    })()}
                                </span>
                            </div>
                            <div className='info-row'>
                                <label>결제 상태</label>
                                <div className='value-with-btn'>
                                    <span className={`payment-val ${getCalculatedStatus.class}`}>
                                        {getCalculatedStatus.label}
                                    </span>
                                    {/* 미납(UNPAID), 차량이 주차장 안에 있는 상태일때만 결제처리 버튼 활성화 */}
                                    {/* <button className={`action-btn-primary ${data.paymentStatus !== 'UNPAID' ? 'disabled' : ''}`}>
                                        결제처리
                                    </button> */}
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