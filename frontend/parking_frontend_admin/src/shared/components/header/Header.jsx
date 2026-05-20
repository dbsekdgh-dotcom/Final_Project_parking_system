import { useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import './header.css'

function pad(n) {
  return String(n).padStart(2, '0')
}

// ── 헤더용 아이콘 (22×22) ────────────────────────────────────────────
function IconHome() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
      <polyline points="9 22 9 12 15 12 15 22" />
    </svg>
  )
}
function IconBuilding() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M6 22V4a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v18" />
      <path d="M6 12h4M6 16h4M6 8h4M14 12h2M14 16h2M14 8h2" />
    </svg>
  )
}
function IconList() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <line x1="8" y1="6"  x2="21" y2="6"  />
      <line x1="8" y1="12" x2="21" y2="12" />
      <line x1="8" y1="18" x2="21" y2="18" />
      <line x1="3" y1="6"  x2="3.01" y2="6"  />
      <line x1="3" y1="12" x2="3.01" y2="12" />
      <line x1="3" y1="18" x2="3.01" y2="18" />
    </svg>
  )
}
function IconTag() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z" />
      <line x1="7" y1="7" x2="7.01" y2="7" />
    </svg>
  )
}
function IconCheck() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
      <polyline points="22 4 12 14.01 9 11.01" />
    </svg>
  )
}
function IconUsers() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
      <circle cx="9" cy="7" r="4" />
      <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
      <path d="M16 3.13a4 4 0 0 1 0 7.75" />
    </svg>
  )
}
function IconArrow() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <line x1="5" y1="12" x2="19" y2="12" />
      <polyline points="12 5 19 12 12 19" />
    </svg>
  )
}
function IconStore() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z" />
      <line x1="3" y1="6" x2="21" y2="6" />
      <path d="M16 10a4 4 0 0 1-8 0" />
    </svg>
  )
}
function IconSettings() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <circle cx="12" cy="12" r="3" />
      <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" />
    </svg>
  )
}

// ── 경로 → 헤더 설정 매핑 ───────────────────────────────────────────
const menuConfig = [
  {
    match: (p) => p === '/admin/dashboard',
    title: '대시보드',
    subtitle: '전체 주차 현황 요약',
    Icon: IconHome,
  },
  {
    match: (p) => p === '/admin/parking-space',
    title: '주차공간',
    subtitle: '구역별 주차 가능 현황',
    Icon: IconBuilding,
  },
  {
    match: (p) => p === '/admin/entry-exit',
    title: '입출차 기록',
    subtitle: '주차 로그 & 결제 상태',
    Icon: IconList,
  },
  {
    match: (p) => p === '/admin/fee',
    title: '요금 설정/조회',
    subtitle: '요금 정책 및 이력 관리',
    Icon: IconTag,
  },
  {
    match: (p) => p.startsWith('/admin/approval'),
    title: '승인 관리',
    subtitle: '승인 요청 및 신고 처리',
    Icon: IconCheck,
  },
  {
    match: (p) => p.startsWith('/admin/user-vehicle'),
    title: '사용자 / 차량',
    subtitle: '회원 및 차량 정보 관리',
    Icon: IconUsers,
  },
  {
    match: (p) => p.startsWith('/admin/action-log'),
    title: '모든 활동 내역',
    subtitle: '입출차 및 관리자 활동 로그',
    Icon: IconArrow,
  },
  {
    match: (p) => p === '/admin/store',
    title: '상가 관리',
    subtitle: '상가 정보 및 할인권 관리',
    Icon: IconStore,
  },
  {
    match: (p) => p.startsWith('/admin/system-setting'),
    title: '시스템 설정',
    subtitle: '운영 정책 및 환경 설정',
    Icon: IconSettings,
  },
]

const defaultMenu = {
  title: '관리자 시스템',
  subtitle: 'Parking Management',
  Icon: IconHome,
}

export default function Header({ pendingApproval = 0, pendingReport = 0, onMenuClick }) {
  const [now, setNow] = useState(() => new Date())
  const location = useLocation()

  useEffect(() => {
    const id = setInterval(() => setNow(new Date()), 1000)
    return () => clearInterval(id)
  }, [])

  const currentMenu =
    menuConfig.find((m) => m.match(location.pathname)) || defaultMenu
  const { title, subtitle, Icon } = currentMenu

  const timeStr = `${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
  const dateStr = new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric', month: '2-digit', day: '2-digit', weekday: 'short',
  }).format(now)

  return (
    <header className="header">
      <div className="header__left">
        <button className="header__menu-btn" onClick={onMenuClick} aria-label="메뉴 열기">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/>
          </svg>
        </button>
        <span className="header__home" aria-hidden>
          <Icon />
        </span>
        <div className="header__titles">
          <h1 className="header__title">{title}</h1>
          <p className="header__subtitle">{subtitle}</p>
        </div>
      </div>

      <div className="header__right">
        <div className="header__notifications">
          <Link to="/admin/approval/approval-request" className="header__approval">
            승인 대기 {pendingApproval}건
          </Link>
          <Link to="/admin/approval/report" className="header__report">
            신고 대기 {pendingReport}건
          </Link>
        </div>
        <div className="header__time-block">
          <div className="header__clock">{timeStr}</div>
          <div className="header__date">{dateStr}</div>
        </div>
      </div>
    </header>
  )
}
