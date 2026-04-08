import React from 'react';
import { openFindEmailModal, openFindPwModal } from '../utils/accountUtils';

const FindAccountButtons = () => {
    return (
        <div className="bottomRow" style={{ display: 'flex', justifyContent: 'center', gap: '10px', marginTop: '20px', fontSize: '14px', color: '#666' }}>
            <button type="button" onClick={() => openFindEmailModal()} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'inherit' }}>
                아이디 찾기
            </button>
            <span>|</span>
            <button type="button" onClick={() => openFindPwModal()} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'inherit' }}>
                비밀번호 찾기
            </button>
        </div>
    );
};

export default FindAccountButtons;