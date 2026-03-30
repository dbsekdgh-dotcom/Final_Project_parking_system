import { useState } from 'react'
import './sidebar.css'

const mainNav = [
  { to: '#dashboard', label: '대시보드', id: 'dashboard' },
  { to: '#parking-space', label: '주차공간', id: 'parking-space' },
  { to: '#entry-exit', label: '입출차 기록', id: 'entry-exit' },
  { to: '#fee', label: '요금 설정/조회', id: 'fee' },
  { to: '#live-video', label: '실시간영상', id: 'live-video' },
  { to: '#video-records', label: '영상 기록', id: 'video-records' },
  { to: '#realtime-io', label: '실시간 입출차', id: 'realtime-io' },
  { to: '#approval', label: '승인 관리', id: 'approval', badge: 4 },
  { to: '#user-vehicle', label: '사용자 / 차량', id: 'user-vehicle' },
]

const bottomNav = [
  { to: '#admin', label: '관리자', id: 'admin' },
  { to: '#settings', label: '시스템 설정', id: 'settings' },
]

function IconHome() {
  return (
    <svg className="sidebar__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
      <polyline points="9 22 9 12 15 12 15 22" />
    </svg>
  )
}

function IconBuilding() {
  return (
    <svg className="sidebar__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M6 22V4a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v18" />
      <path d="M6 12h4M6 16h4M6 8h4M14 12h2M14 16h2M14 8h2" />
    </svg>
  )
}

function IconList() {
  return (
    <svg className="sidebar__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <line x1="8" y1="6" x2="21" y2="6" />
      <line x1="8" y1="12" x2="21" y2="12" />
      <line x1="8" y1="18" x2="21" y2="18" />
      <line x1="3" y1="6" x2="3.01" y2="6" />
      <line x1="3" y1="12" x2="3.01" y2="12" />
      <line x1="3" y1="18" x2="3.01" y2="18" />
    </svg>
  )
}

function IconTag() {
  return (
    <svg className="sidebar__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z" />
      <line x1="7" y1="7" x2="7.01" y2="7" />
    </svg>
  )
}

function IconPlay() {
  return (
    <svg className="sidebar__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <polygon points="5 3 19 12 5 21 5 3" />
    </svg>
  )
}

function IconFile() {
  return (
    <svg className="sidebar__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
      <polyline points="14 2 14 8 20 8" />
    </svg>
  )
}

function IconArrow() {
  return (
    <svg className="sidebar__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <line x1="5" y1="12" x2="19" y2="12" />
      <polyline points="12 5 19 12 12 19" />
    </svg>
  )
}

function IconCheck() {
  return (
    <svg className="sidebar__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
      <polyline points="22 4 12 14.01 9 11.01" />
    </svg>
  )
}

function IconUsers() {
  return (
    <svg className="sidebar__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
      <circle cx="9" cy="7" r="4" />
      <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
      <path d="M16 3.13a4 4 0 0 1 0 7.75" />
    </svg>
  )
}

function IconAdmin() {
  return (
    <svg className="sidebar__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
      <circle cx="9" cy="7" r="4" />
      <path d="M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" />
    </svg>
  )
}

function IconSettings() {
  return (
    <svg className="sidebar__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <circle cx="12" cy="12" r="3" />
      <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" />
    </svg>
  )
}

const iconsById = {
  dashboard: IconHome,
  'parking-space': IconBuilding,
  'entry-exit': IconList,
  fee: IconTag,
  'live-video': IconPlay,
  'video-records': IconFile,
  'realtime-io': IconArrow,
  approval: IconCheck,
  'user-vehicle': IconUsers,
  admin: IconAdmin,
  settings: IconSettings,
}

export default function Sidebar() {
  const [activeId, setActiveId] = useState('dashboard')

  const renderLink = (item) => {
    const Icon = iconsById[item.id] || IconHome
    const isActive = activeId === item.id
    return (
      <li key={item.id} className="sidebar__item">
        <a
          href={item.to}
          className={`sidebar__link${isActive ? ' sidebar__link--active' : ''}`}
          onClick={() => setActiveId(item.id)}
        >
          <Icon />
          <span className="sidebar__link-text">{item.label}</span>
          {item.badge != null && <span className="sidebar__badge">{item.badge}</span>}
        </a>
      </li>
    )
  }

  return (
    <aside className="sidebar">
      <div className="sidebar__top">
        <a href="#/" className="sidebar__brand">
          <span className="sidebar__logo" aria-hidden>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
              <path d="M4 4h6v6H4V4zm10 0h6v6h-6V4zM4 14h6v6H4v-6zm10 0h6v6h-6v-6z" fill="#fff" opacity="0.95" />
            </svg>
          </span>
          <span className="sidebar__title">Parking</span>
        </a>
        <button type="button" className="sidebar__exit" aria-label="나가기">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
            <polyline points="16 17 21 12 16 7" />
            <line x1="21" y1="12" x2="9" y2="12" />
          </svg>
        </button>
      </div>

      <div className="sidebar__user">
        <span className="sidebar__user-name">Admin 1</span>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <polyline points="6 9 12 15 18 9" />
        </svg>
      </div>

      <ul className="sidebar__nav">{mainNav.map((item) => renderLink(item))}</ul>

      <ul className="sidebar__bottom">{bottomNav.map((item) => renderLink(item))}</ul>
    </aside>
  )
}
