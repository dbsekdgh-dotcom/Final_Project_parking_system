import React from 'react'
import './ParkingControlModal.css'

const ParkingControlModal = ({isOpen, onClose, selectedSpace, onBlock, onUnblock, onChangeType}) => {
    if(!isOpen || !selectedSpace) return null

    return (
        <div className='admin-modal-overlay' onClick={onClose}>
            {/* onClick={onClose}: 배경 클릭 시 모달 닫힘 */}
            <div className='admin-modal' onClick={(e)=>e.stopPropagation()}>
                {/* stopPropagation: 모달 본체 클릭시 닫히지 않게 방지 */}
                <div className='modal-header'>
                    <h3>구역 제어: {selectedSpace.spaceCode}</h3>
                    <button className='close-x-btn' onClick={onClose}>&times;</button>
                </div>

                <div className='modal-body'>
                    <p>현재 상태: <strong>{selectedSpace.status}</strong></p>
                    <div className='type-badges'>
                        {selectedSpace.isDisabled && <span className='badge disabled'>장애인 전용</span>}
                        {selectedSpace.isEvCharge && <span className='badge ev'>전기차 충전</span>}
                        {!selectedSpace.isDisabled && !selectedSpace.isEvCharge && <span className='badge general'>일반 구역</span>}
                    </div>
                </div>

                <div className='modal-section'>
                    <h4>구역 타입 변경</h4>
                    <div className='button-group'>
                        <button onClick={()=>onChangeType(selectedSpace.id, 'SET_DISABLED')}
                            disabled={selectedSpace.isDisabled}>장애인석</button>
                        <button onClick={()=>onChangeType(selectedSpace.id, 'SET_EV')}
                            disabled={selectedSpace.isEvCharge}>전기차석</button>
                        <button onClick={()=>onChangeType(selectedSpace.id, 'SET_GENERAL')}
                            disabled={!selectedSpace.isDisabled && !selectedSpace.isEvCharge}>일반석</button>
                    </div>
                </div>

                <div className='modal-section'>
                    <h4>상태 제어</h4>
                    <div className='button-group'>
                        {selectedSpace.status === 'BLOCKED' ? (
                            <button className='btn-unblock' onClick={()=>onUnblock(selectedSpace.id)}>차단 해제</button>
                        ) : (
                            <button className='btn-block' onClick={()=>onBlock(selectedSpace.id)}>구역 차단</button>
                        )}
                    </div>
                </div>
            </div>
        </div>
    )
}

export default ParkingControlModal