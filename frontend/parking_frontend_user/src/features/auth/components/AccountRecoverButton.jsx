import React from 'react';
// 경로 끝에 .js를 붙여서 Vite가 명확하게 파일을 찾도록 합니다.
import { handleAccountRecover } from '../utils/accountUtils.js';

const AccountRecoverButton = ({ className, style, buttonText = "계정 복구", initialData = {} }) => {
    return (
        <button 
            type="button" 
            className={className} 
            style={style} 
            onClick={() => handleAccountRecover(initialData)}
        >
            {buttonText}
        </button>
    );
};

export default AccountRecoverButton;