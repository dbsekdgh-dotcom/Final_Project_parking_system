import { useNavigate, useLocation, Outlet } from 'react-router-dom';
import { useMemo } from 'react';
import SubHeader from '../../approval/components/SubHeader';

const TABS = [
    { id: 'admin', label: '관리자 활동내역', path: '/admin/action-log/admin' },
    { id: 'user', label: '사용자 활동내역', path: '/admin/action-log/user' },
];

export default function ActionLogLayout() {
    const navigate = useNavigate();
    const { pathname } = useLocation();

    const activeTab = useMemo(
        () => TABS.find(t => pathname.endsWith(t.id))?.id ?? TABS[0].id,
        [pathname]
    );

    return (
        <>
            <SubHeader
                tabs={TABS}
                activeTab={activeTab}
                onChange={(id) => {
                    const tab = TABS.find(t => t.id === id);
                    if (tab) navigate(tab.path);
                }}
            />
            <Outlet />
        </>
    );
}