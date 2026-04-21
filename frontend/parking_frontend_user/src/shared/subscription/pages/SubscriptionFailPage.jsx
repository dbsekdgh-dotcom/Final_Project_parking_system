import { useEffect, useRef } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import Swal from 'sweetalert2';

export default function SubscriptionFailPage() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const processed = useRef(false);

    useEffect(() => {
        if (processed.current) return;
        processed.current = true;

        const message = searchParams.get('message') || '결제가 취소되었습니다.';

        Swal.fire({
            icon: 'error',
            title: '결제 실패',
            text: decodeURIComponent(message),
            confirmButtonText: '확인',
            confirmButtonColor: '#d33',
        }).then(() => navigate('/subscription', { replace: true }));
    }, []);

    return <div style={{ padding: '2rem', textAlign: 'center' }}>처리 중...</div>;
}
