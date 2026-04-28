import React, { useState } from 'react'
import Keypad from '../../../shared/components/keypad/Keypad'
import { useNavigate } from 'react-router-dom'
import useStoreStore from '../../../store/useStoreStore';
import { storeLogin, getWallets } from '../api/storeApi';
import './StoreLoginPage.css';

export default function StoreLoginPage() {
    const navigate = useNavigate();
    const { setStoreInfo, setWallets } = useStoreStore();
    const [ password, setPassword ] = useState('');
    const [ error, setError ] = useState('');

    const handleKey = (k) => setPassword(prev => prev.length < 6 ? prev + String(k) : prev);
    const handleDelete = () => setPassword(prev => prev.slice(0, -1));
    const handleClear = () => setPassword('');

    const handleConfirm = async () => {
        if (!password) return;
        try {
            const res = await storeLogin(password);
            localStorage.setItem('storeToken', res.token);
            setStoreInfo(res.storeId, res.storeName, res.token);
            const ws = await getWallets();
            setWallets(ws);
            navigate('/store/main');
        } catch {
            setError('비밀번호가 올바르지 않습니다.')
        }
    };

    return (
        <div className="store-login-wrapper">
            <h2 className="store-login-title">상가 로그인</h2>
            <p className="store-login-label">비밀번호 (숫자만)</p>
            <div className="store-login-display">
                {'●'.repeat(password.length) || '------'}
            </div>
            {error && <p className="store-login-error">{error}</p>}
            <Keypad onKeyClick={handleKey} onDeleteClick={handleDelete} onClearClick={handleClear} />
            <div className="store-login-actions">
                <button className="btn-back" onClick={() => navigate('/')}>돌아가기</button>
                <button className="btn-confirm" onClick={handleConfirm} disabled={!password}>확인</button>
            </div>
        </div>
    )
}
