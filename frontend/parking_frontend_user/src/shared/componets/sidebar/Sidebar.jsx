import React, {useState} from 'react'
import { NavLink } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import './sidebar-css.css'
import AuthAccountLinkWidget from '../../../features/auth/components/AuthAccountLinkWidget'
import LogoutButton from '../../../features/auth/components/LogoutButton'
// [추가] 회원 탈퇴 버튼 임포트 (auth/components 폴더 내 위치)
import WithdrawButton from '../../../features/auth/components/WithdrawButton'
import { fetchUserStatus } from '../../../features/apply/api/applyApi'
import Noti from "../../../features/notification/Notification";
import { fetchUnreadCount } from '../../../features/notification/api/notificationApi'; 
import api from '../../../features/auth/api/axios';


const navItems = [
  { to: '/dashboard', label: '홈', end: true, icon: HomeIcon },
  { to: '/subscription', label: '정기권', icon: TicketIcon },
  { to: '/reservation', label: '방문 예약', icon: CalendarIcon },
  { to: '/mypage', label: '마이페이지', icon: UserIcon },
  { to: '/report', label: '민원/신고', icon: AlertIcon },
]

const BADGE_CONFIG = {
  RESIDENT: { label: "입주민",   cls: "sidebar__badge--resident" },
  PENDING:  { label: "신청 중",  cls: "sidebar__badge--pending" },
  NONE:     { label: "일반 회원", cls: "sidebar__badge--none" },
};

export function Sidebar() {
  const [isNotiOpen, setIsNotiOpen] = useState(false);
  //유저 상태 정보
  const { data: statusData } = useQuery({
    queryKey: ["userStatus"],
    queryFn: fetchUserStatus,
  });

  //안 읽은 알림 갯수 쿼리
  const { data: unreadCount = 0, refetch: refetchCount } = useQuery({
    queryKey: ["unreadNotificationCount"],
    queryFn: async () =>{
      const response = await api.get('/api/notifications/unread-count');
      return response.data;
    },
    refetchOnWindowFocus: true,
  });

  const handleBellClick =(e) => {
    e.stopPropagation();
    setIsNotiOpen(!isNotiOpen);
  }


  // 쿼리 데이터 우선, 없으면 localStorage 폴백 (로그인 직후 캐시 없을 때)
  const memberStatus = statusData?.userStatus ?? localStorage.getItem("userStatus") ?? "NONE";
  const unitNo = localStorage.getItem("unitNo");

  const badge = BADGE_CONFIG[memberStatus] ?? BADGE_CONFIG.NONE;
  const metaText = memberStatus === "RESIDENT" && unitNo ? `${unitNo}호` : "";

  return (
    <aside className="sidebar">
     <div className="sidebar__brand" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', width: '100%' }}>
        <div style={{ display: 'flex', alignItems: 'center' }}>
          <CarIcon className="sidebar__brand-icon" />
          <span className="sidebar__brand-title">Smart Parking</span>
        </div>

        {/* 🔔 [추가] 알림 버튼 위치 */}
        <div style={{ position: 'relative', marginRight: '10px' }}>
          <button 
            onClick={(e) => { e.stopPropagation(); setIsNotiOpen(!isNotiOpen)}}
            className="sidebar__noti-btn" >
            🔔
            {unreadCount > 0 && (
              <span className="sidebar__noti-badge">
                {unreadCount > 99 ? '99+' : unreadCount}
              </span>
            )}
        </button>
          
          {/* 알림창: 사이드바 옆으로 튀어나오게 설정 */}
          {isNotiOpen && (
          <div 
            style={{ position: 'absolute', left: '100%', top: '0', marginLeft: '10px', zIndex: 999 }}
            onClick={(e) => e.stopPropagation()} // 👈 알림창 내부 클릭 시에도 닫히지 않게 보호!
          >
            <Noti 
              onMutationSuccess={refetchCount} 
              onClose={() => setIsNotiOpen(false)} 
            />
         </div>
          )}
        </div>
      </div>

      <div className="sidebar__user">
        <div className="sidebar__user-text">
          <AuthAccountLinkWidget metaText={metaText} badge={badge} />
        </div>
      </div>

      <div className="sidebar__divider" />

      <nav className="sidebar__nav" aria-label="주 메뉴">
        {navItems
          .map(({ to, label, icon: Icon, end }) => (
            <NavLink
              key={to}
              to={to}
              end={end}
              className={({ isActive }) =>
                `sidebar__link${isActive ? ' sidebar__link--active' : ''}`
              }
            >
              <Icon className="sidebar__link-icon" />
              {label}
              <span className="sidebar__link-chevron" aria-hidden>
                ›
              </span>
            </NavLink>
          ))}
      </nav>

      <div className="sidebar__footer">
        <div className="sidebar__divider" />
        
        {/* 로그아웃 버튼 */}
        <LogoutButton className="sidebar__logout">
          로그아웃
          <LogoutIcon />
        </LogoutButton>

        {/* [추가] 회원 탈퇴 버튼 배치 (로그아웃 아래) */}
        <WithdrawButton />
      </div>
    </aside>
  )
}

// --- 아래는 아이콘 컴포넌트들입니다 (수정 없음) ---

function CarIcon({ className }) {
  return (
    <svg className={className} width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round">
      <path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2" />
      <circle cx="7" cy="17" r="2" />
      <path d="M9 17h6" />
      <circle cx="17" cy="17" r="2" />
    </svg>
  )
}

function HomeIcon({ className }) {
  return (
    <svg className={className} width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round">
      <path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
      <polyline points="9 22 9 12 15 12 15 22" />
    </svg>
  )
}

function TicketIcon({ className }) {
  return (
    <svg className={className} width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round">
      <path d="M2 9a3 3 0 0 1 0 6v2a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-2a3 3 0 0 1 0-6V7a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2Z" />
      <path d="M13 5v2" />
      <path d="M13 17v2" />
      <path d="M13 11v2" />
    </svg>
  )
}

function CalendarIcon({ className }) {
  return (
    <svg className={className} width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round">
      <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
      <line x1="16" y1="2" x2="16" y2="6" />
      <line x1="8" y1="2" x2="8" y2="6" />
      <line x1="3" y1="10" x2="21" y2="10" />
    </svg>
  )
}

function UserIcon({ className }) {
  return (
    <svg className={className} width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round">
      <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2" />
      <circle cx="12" cy="7" r="4" />
    </svg>
  )
}

function AlertIcon({ className }) {
  return (
    <svg className={className} width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round">
      <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z" />
      <line x1="12" y1="9" x2="12" y2="13" />
      <line x1="12" y1="17" x2="12.01" y2="17" />
    </svg>
  )
}

function LogoutIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
      <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
      <polyline points="16 17 21 12 16 7" />
      <line x1="21" y1="12" x2="9" y2="12" />
    </svg>
  )
}