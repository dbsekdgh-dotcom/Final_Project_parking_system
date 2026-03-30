import { useEffect, useState } from 'react'
import './header.css'

function pad(n) {
  return String(n).padStart(2, '0')
}

export default function Header() {
  const [now, setNow] = useState(() => new Date())

  useEffect(() => {
    const id = setInterval(() => setNow(new Date()), 1000)
    return () => clearInterval(id)
  }, [])

  const timeStr = `${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`

  const dateStr = new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    weekday: 'short',
  }).format(now)

  return (
    <header className="header">
      <div className="header__left">
        <span className="header__home" aria-hidden>
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
            <polyline points="9 22 9 12 15 12 15 22" />
          </svg>
        </span>
        <div className="header__titles">
          <h1 className="header__title">대시보드</h1>
          <p className="header__subtitle">description</p>
        </div>
      </div>
      <div className="header__right">
        <a href="#approval" className="header__approval">
          승인 대기 4건
        </a>
        <div className="header__time-block">
          <div className="header__clock">{timeStr}</div>
          <div className="header__date">{dateStr}</div>
        </div>
      </div>
    </header>
  )
}
