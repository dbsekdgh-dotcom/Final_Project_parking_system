 import { useNavigate, useLocation, Outlet } from 'react-router-dom';
  import { useMemo } from 'react';
  import SubHeader from '../../approval/components/SubHeader';  // 공용 SubHeader 재사용

  const TABS = [
      { id: 'status',             label: '상태값 변경',    path: '/admin/system-setting/status' },
      { id: 'reservation-policy', label: '방문예약 정책',  path: '/admin/system-setting/reservation-policy' },
  ];

  export default function SystemSettingLayout() {
      const navigate     = useNavigate();
      const { pathname } = useLocation();

      const activeTab = useMemo(
          () => TABS.find(t => pathname.includes(t.id))?.id ?? TABS[0].id,
          [pathname]
      );

      return (
          <div className="arp">  {/* 승인관리와 동일한 wrapper 클래스 — CSS 재사용 */}
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