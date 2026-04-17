import './SubHeader.css';

/**
 * SubHeader — 공용 탭 서브 헤더 컴포넌트
 *
 * Props:
 *   tabs       {Array}  - [{ id, label, badge?, badgeVariant? }]
 *   activeTab  {string} - 현재 활성 탭 id
 *   onChange   {fn}     - (tabId: string) => void
 *
 * 사용 예시:
 *   const TABS = [
 *     { id: 'approval', label: '승인 요청', badge: 3 },
 *     { id: 'report',   label: '신고 관리', badge: 2, badgeVariant: 'warning' },
 *   ];
 *   <SubHeader tabs={TABS} activeTab={activeTab} onChange={setActiveTab} />
 */
export default function SubHeader({ tabs = [], activeTab, onChange }) {
  return (
    <nav className="sub-header" role="tablist" aria-label="페이지 탭">
      {tabs.map(tab => (
        <button
          key={tab.id}
          role="tab"
          aria-selected={activeTab === tab.id}
          className={`sub-header__tab${activeTab === tab.id ? ' sub-header__tab--active' : ''}`}
          onClick={() => onChange?.(tab.id)}
        >
          {tab.label}
          {tab.badge > 0 && (
            <span
              className={`sub-header__badge${
                tab.badgeVariant ? ` sub-header__badge--${tab.badgeVariant}` : ''
              }`}
            >
              {tab.badge}
            </span>
          )}
        </button>
      ))}
    </nav>
  );
}
