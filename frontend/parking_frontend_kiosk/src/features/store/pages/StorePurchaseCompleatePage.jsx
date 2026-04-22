import { useNavigate } from "react-router-dom";


export default function StorePurchaseCompletePage(){
    const navigate = useNavigate();
     return (
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100vh' }}>
        <div style={{ fontSize: '60px' }}>✓</div>
        <h2>할인권 구매가 완료되었습니다.</h2>
        <p style={{ color: '#888' }}>구매하신 할인권이 지갑에 추가되었습니다.</p>
        <button
          style={{ marginTop: '32px', padding: '16px 40px', background: '#000', color: '#fff' }}
          onClick={() => navigate('/store/main')}
        >
          상가 홈으로
        </button>
      </div>
    );
}