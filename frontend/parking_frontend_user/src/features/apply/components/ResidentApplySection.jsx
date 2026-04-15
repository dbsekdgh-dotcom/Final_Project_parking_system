import React, { useState } from 'react';
import Swal from 'sweetalert2';
import { useUnitStatus, useApplyResident } from '../hooks/useApply';
import './ResidentApplySection.css';

const STATUS_CONFIG = {
    AVAILABLE: { label: '신청 가능', bg: '#e3f2fd', color: '#1565c0', clickable: true },
    PENDING:   { label: '신청 중',   bg: '#fff8e1', color: '#f57f17', clickable: false },
    OCCUPIED:  { label: '거주 중',   bg: '#f5f5f5', color: '#9e9e9e', clickable: false },
};

const ResidentApplySection = ({ memberStatus }) => {
    const [selectedUnit, setSelectedUnit] = useState(null); // { householdId, unitNo }

    const { data: unitStatuses = [], isLoading } = useUnitStatus();
    const applyMutation = useApplyResident(() => setSelectedUnit(null));

    const isAlreadyApplied = memberStatus === 'PENDING' || memberStatus === 'RESIDENT';

    const handleCellClick = (unit) => {
        if (isAlreadyApplied) return;
        const config = STATUS_CONFIG[unit.status];
        if (!config?.clickable) return;
        setSelectedUnit(prev =>
            prev?.unitNo === unit.unitNo ? null : { householdId: unit.householdId, unitNo: unit.unitNo }
        );
    };

    const handleApply = () => {
        if (!selectedUnit) {
            Swal.fire({ icon: 'warning', title: '호수를 선택해주세요.', confirmButtonColor: '#3085d6' });
            return;
        }
        Swal.fire({
            icon: 'question',
            title: `${selectedUnit.unitNo}호 입주민 신청`,
            text: '선택한 호수로 입주민 등록을 신청하시겠습니까?',
            showCancelButton: true,
            confirmButtonText: '신청하기',
            cancelButtonText: '취소',
            confirmButtonColor: '#3085d6',
        }).then((result) => {
            if (result.isConfirmed) applyMutation.mutate(selectedUnit.householdId);
        });
    };

    return (
        <div className="apply-section">
            <h3 className="apply-section__title">입주민 등록 신청</h3>

            {isAlreadyApplied ? (
                <p className="apply-section__notice">
                    {memberStatus === 'RESIDENT'
                        ? '이미 입주민으로 등록되어 있습니다.'
                        : '현재 입주민 등록 신청이 접수되어 있습니다. 관리자 승인을 기다려주세요.'}
                </p>
            ) : (
                <p className="apply-section__hint">신청 가능한 호수를 클릭하여 선택하세요.</p>
            )}

            {isLoading ? (
                <div className="apply-loading">불러오는 중...</div>
            ) : (
                <div className="unit-grid">
                    {unitStatuses.map((unit) => {
                        const config = STATUS_CONFIG[unit.status] ?? STATUS_CONFIG.OCCUPIED;
                        const isSelected = selectedUnit?.unitNo === unit.unitNo;
                        return (
                            <div
                                key={unit.unitNo}
                                className={[
                                    'unit-cell',
                                    !isAlreadyApplied && config.clickable ? 'unit-cell--clickable' : '',
                                    isSelected ? 'unit-cell--selected' : '',
                                ].join(' ')}
                                style={{ background: isSelected ? '#cfe0ff' : config.bg }}
                                onClick={() => handleCellClick(unit)}
                                title={config.clickable ? '클릭하여 선택' : config.label}
                            >
                                <span className="unit-no">{unit.unitNo}호</span>
                                <span className="unit-status" style={{ color: isSelected ? '#233b6e' : config.color }}>
                                    {isSelected ? '선택됨' : config.label}
                                </span>
                            </div>
                        );
                    })}
                </div>
            )}

            <div className="unit-legend">
                {Object.entries(STATUS_CONFIG).map(([key, cfg]) => (
                    <span key={key} className="legend-item" style={{ background: cfg.bg, color: cfg.color }}>
                        {cfg.label}
                    </span>
                ))}
            </div>

            <div className="apply-footer">
                {selectedUnit && (
                    <span className="apply-selected-label">{selectedUnit.unitNo}호 선택됨</span>
                )}
                <button
                    className="btn-apply"
                    onClick={handleApply}
                    disabled={applyMutation.isPending || !selectedUnit || isAlreadyApplied}
                >
                    {applyMutation.isPending ? '신청 중...' : '입주민 등록 신청'}
                </button>
            </div>
        </div>
    );
};

export default ResidentApplySection;
