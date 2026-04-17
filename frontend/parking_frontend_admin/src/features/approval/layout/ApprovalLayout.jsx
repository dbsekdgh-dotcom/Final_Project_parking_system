import { useNavigate, useLocation, Outlet } from 'react-router-dom';
import { useMemo } from 'react';
import SubHeader from '../components/SubHeader';
import '../approval-request/pages/ApprovalRequestPage.css';

const TABS = [
  { id: 'approval-request', label: '승인 요청', path: '/admin/approval/approval-request' },
  { id: 'report',           label: '신고 관리', path: '/admin/approval/report', badgeVariant: 'warning' },
];

export default function ApprovalLayout() {
  const navigate     = useNavigate();
  const { pathname } = useLocation();

  const activeTab = useMemo(
    () => TABS.find(t => pathname.includes(t.id))?.id ?? TABS[0].id,
    [pathname]
  );

  return (
    <div className="arp">
      <SubHeader
        tabs={TABS}
        activeTab={activeTab}
        onChange={(id) => {
          const tab = TABS.find(t => t.id === id);
          if (tab) navigate(tab.path);
        }}
      />
      <Outlet />
    </div>
  );
}
