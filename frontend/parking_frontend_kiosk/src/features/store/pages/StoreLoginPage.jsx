import React, { useEffect, useState } from 'react'
import Keypad from '../../../shared/components/keypad/Keypad'
import { useNavigate } from 'react-router-dom'
import useStoreStore from '../../../store/useStoreStore';
import { storeLogin, getWallets, getStoreTestHint } from '../api/StoreApi';
import './StoreLoginPage.css';

export default function StoreLoginPage() {
    const navigate = useNavigate();
    const { setStoreInfo, setWallets } = useStoreStore();
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [hint, setHint] = useState(null);

    useEffect(() => {
        getStoreTestHint(2)
            .then(data => setHint(data))
            .catch(() => {});
    }, []);

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
        <div className="store-page-root">
            <div className="store-login-wrapper">
                <h2 className="store-login-title">상가 로그인</h2>
                {hint && (
                    <div style={{
                        margin: '0 0 16px',
                        padding: '10px 14px',
                        background: 'rgba(234, 179, 8, 0.12)',
                        border: '1px dashed rgba(234, 179, 8, 0.5)',
                        borderRadius: '8px',
                        fontSize: '0.8rem',
                        color: '#a16207',
                        textAlign: 'center',
                        lineHeight: '1.6',
                    }}>
                        <strong>테스트용 계정</strong><br />
                        상가명: {hint.storeName} &nbsp;|&nbsp; 비밀번호: <strong>{hint.password}</strong>
                    </div>
                )}
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
        </div>
    )
}
