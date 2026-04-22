import React, { useState } from 'react'
import Keypad from '../../../shared/components/keypad/Keypad'
import { useNavigate } from 'react-router-dom'
import useStoreStore from '../../../store/useStoreStore';
import { storeLogin, getWallets } from '../api/StoreApi';

export default function StoreLoginPage() {
    const navigate = useNavigate();
    const { setStoreInfo, setWallets } = useStoreStore();
    const [ password,setPassword ] = useState('');
    const [ error, setError ] = useState('');

    const handleKey = (k) => setPassword(prev => prev.length < 6 ? prev + String(k) : prev);
    const handleDelete = () => setPassword(prev => prev.slice(0, -1));
    const handleClear = () => setPassword('');

    const handleConfirm = async () => {
        if (!password) return;
        try{
            const res = await storeLogin(password);
            localStorage.setItem('storeToken', res.token);
            setStoreInfo(res.storeId, res.storeName, res.token);
            const ws = await getWallets();
            setWallets(ws);
            navigate('/store/main');
        }catch{
            setError('비밀번호가 올바르지 않습니다.')
        }
    };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', padding: '40px' }}>
        <h2>상가 로그인</h2>
        <p style={{ color: '#888' }}>비밀번호 (숫자만)</p>
        <div style={{ fontSize: '28px', letterSpacing: '12px', margin: '16px 0', minHeight: '40px' }}>
          {'●'.repeat(password.length) || '------'}
        </div>
        {error && <p style={{ color: 'red', fontSize: '14px' }}>{error}</p>}
        <Keypad onKeyClick={handleKey} onDeleteClick={handleDelete} onClearClick={handleClear} />
        <div style={{ display: 'flex', gap: '16px', marginTop: '24px' }}>
          <button onClick={() => navigate('/')}>돌아가기</button>
          <button onClick={handleConfirm} disabled={!password}>확인</button>
        </div>
      </div>
  )
}

