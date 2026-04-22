import { useNavigate, useLocation, Outlet } from 'react-router-dom';
import { useMemo } from 'react';
import SubHeader from '../components/SubHeader';
import '../../approval/approval-request/pages/ApprovalRequestPage.css';

const TABS = [
  { id: 'user', label: '사용자 관리', path: '/admin/user-vehicle/user' },
  { id: 'vehicle', label: '차량 관리', path: '/admin/user-vehicle/vehicle' },
  { id: 'blacklist', label: '블랙 리스트', path: '/admin/user-vehicle/blacklist' },
  { id: 'reservation', label: '방문 예약', path: '/admin/user-vehicle/reservation' },
  { id: 'subscription', label: '정기권', path: '/admin/user-vehicle/subscription' }
];

export default function UserVehicleLayout() {
  const navigate     = useNavigate();
  const { pathname } = useLocation();

  const activeTab = useMemo(
    () => TABS.find(t => pathname.endsWith('/' + t.id))?.id ?? TABS[0].id,
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
