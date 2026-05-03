import { useEffect, useRef, useState } from 'react'
import './sidebar.css'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import adminApi from '../../api/adminApi'
import { useTheme } from '../../context/ThemeContext'
import AdminListModal from './AdminListModal'

const mainNav = [
  { to: '/admin/dashboard', label: '대시보드', id: 'dashboard' },
  { to: '/admin/parking-space', label: '주차공간', id: 'parking-space' },
  { to: '/admin/entry-exit', label: '입출차 기록', id: 'entry-exit' },
  { to: '/admin/fee', label: '요금 설정/조회', id: 'fee' },
  { to: '/admin/approval/approval-request', label: '승인 관리', id: 'approval', activeMatch: '/admin/approval' },
  { to: '/admin/user-vehicle/user', label: '사용자 / 차량', id: 'user-vehicle' },
  { to: '/admin/action-log', label: '모든 활동 내역', id: 'realtime-io', activeMatch: '/admin/action-log' },
  { to: '/admin/store', label: '상가 관리', id: 'store' },
]

const bottomNav = [
  { to: '#admin', label: '관리자', id: 'admin' },
  { to: '/admin/system-setting/status', label: '시스템 설정', id: 'settings', activeMatch: '/admin/system-setting' },
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

function IconSun() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <circle cx="12" cy="12" r="5" />
      <line x1="12" y1="1" x2="12" y2="3" />
      <line x1="12" y1="21" x2="12" y2="23" />
      <line x1="4.22" y1="4.22" x2="5.64" y2="5.64" />
      <line x1="18.36" y1="18.36" x2="19.78" y2="19.78" />
      <line x1="1" y1="12" x2="3" y2="12" />
      <line x1="21" y1="12" x2="23" y2="12" />
      <line x1="4.22" y1="19.78" x2="5.64" y2="18.36" />
      <line x1="18.36" y1="5.64" x2="19.78" y2="4.22" />
    </svg>
  )
}

function IconMoon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
    </svg>
  )
}

function IconStore() {
  return (
    <svg className="sidebar__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z" />
      <line x1="3" y1="6" x2="21" y2="6" />
      <path d="M16 10a4 4 0 0 1-8 0" />
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
  store: IconStore,
  'entry-exit': IconList,
  fee: IconTag,
  'realtime-io': IconArrow,
  approval: IconCheck,
  'user-vehicle': IconUsers,
  admin: IconAdmin,
  settings: IconSettings,
}

export default function Sidebar({ pendingApproval = 0 }) {
  const location = useLocation()
  const [adminName, setAdminName] = useState('Admin')
  const { theme, toggle } = useTheme()
  const navigate = useNavigate()
  const [showAdminModal, setShowAdminModal] = useState(false)
  const [popoverPos, setPopoverPos] = useState({ left: 0, bottom: 0 })
  const adminBtnRef = useRef(null)

  useEffect(() => {
    const saved = localStorage.getItem('adminName')
    if (saved) setAdminName(saved)
  }, [])

  const renderLink = (item) => {
    const Icon = iconsById[item.id] || IconHome
    const isActive = item.activeMatch
      ? location.pathname.startsWith(item.activeMatch)
      : location.pathname === item.to
    
    if (item.id === 'admin') {
      const handleAdminClick = () => {
        if (adminBtnRef.current) {
          const rect = adminBtnRef.current.getBoundingClientRect()
          setPopoverPos({
            left: rect.right + 8,
            bottom: window.innerHeight - rect.bottom,
          })
        }
        setShowAdminModal(prev => !prev)
      }
      return (
        <li key={item.id} className="sidebar__item">
          <button ref={adminBtnRef} className="sidebar__link" onClick={handleAdminClick}>
            <Icon />
            <span className="sidebar__link-text">{item.label}</span>
          </button>
        </li>
      )
    }

    const badge = item.id === 'approval' ? pendingApproval : null

    return (
      <li key={item.id} className="sidebar__item">
        <Link
          to={item.to}
          className={`sidebar__link${isActive ? ' sidebar__link--active' : ''}`}
        >
          <Icon />
          <span className="sidebar__link-text">{item.label}</span>
          {badge > 0 && <span className="sidebar__badge">{badge}</span>}
        </Link>
      </li>
    )
  }

  const handleLogout = async () => {
    if (!window.confirm('로그아웃 하시겠습니까?')) return
    try {
      await adminApi.post('/logout')
    } catch (error) {
      console.error('로그아웃 중 오류 발생:', error)
    } finally {
      sessionStorage.removeItem('accessToken')
      localStorage.removeItem('adminName')
      alert('로그아웃 되었습니다.')
      navigate('/admin')
    }
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
        <div className='sidebar__top-actions'>
          <button type='button' className='sidebar__theme-toggle' aria-label='테마 변경' onClick={toggle}>
            {theme === 'dark' ? <IconSun /> : <IconMoon />}
          </button>
          <button type="button" className="sidebar__exit" aria-label="나가기" onClick={handleLogout}>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
              <polyline points="16 17 21 12 16 7" />
              <line x1="21" y1="12" x2="9" y2="12" />
            </svg>
          </button>
        </div>
      </div>

      <div className="sidebar__user">
        <span className="sidebar__user-name">{adminName} 관리자님</span>
      </div>

      <ul className="sidebar__nav">{mainNav.map(renderLink)}</ul>
      <ul className="sidebar__bottom">{bottomNav.map(renderLink)}</ul>
      {showAdminModal && (
        <AdminListModal
          onClose={() => setShowAdminModal(false)}
          position={popoverPos}
          excludeRef={adminBtnRef}
        />
      )}
    </aside>
  )
}
