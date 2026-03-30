import './header-css.css'

export function Header() {
  return (
    <header className="dashboard-header">
      <div className="dashboard-header__intro">
        <span className="dashboard-header__eyebrow">실시간 현황</span>
        <h1 className="dashboard-header__title">Smart Parking</h1>
      </div>

      <div className="dashboard-header__stats">
        <div className="dashboard-header__stat">
          <span className="dashboard-header__stat-value">24 / 60</span>
          <span className="dashboard-header__stat-label">가용 주차면</span>
        </div>
        <div className="dashboard-header__stat">
          <span className="dashboard-header__stat-value">40%</span>
          <span className="dashboard-header__stat-label">현재 점유율</span>
        </div>
        <div className="dashboard-header__stat">
          <span className="dashboard-header__stat-value dashboard-header__stat-value--accent">
            B1
          </span>
          <span className="dashboard-header__stat-label">여유 있는 층</span>
        </div>
      </div>
    </header>
  )
}
