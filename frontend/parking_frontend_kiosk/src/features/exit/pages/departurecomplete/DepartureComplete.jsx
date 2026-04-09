import "./DepartureComplete.css";

export default function DepartureComplete({ onHome }) {
  return (
    <div className="dc-backdrop">
      <div className="dc-modal">
        <div className="dc-icon-wrap">
          <svg
            className="dc-check-icon"
            viewBox="0 0 56 56"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <circle
              cx="28"
              cy="28"
              r="26"
              stroke="currentColor"
              strokeWidth="2.5"
            />
            <path
              d="M17 28.5L24.5 36L39 21"
              stroke="currentColor"
              strokeWidth="2.5"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </svg>
        </div>

        <h2 className="dc-title">출차가 완료되었습니다</h2>
        <p className="dc-subtitle">이용해 주셔서 감사합니다</p>

        <div className="dc-divider" />

        <button className="dc-btn" onClick={onHome}>
          홈으로 돌아가기
        </button>
      </div>
    </div>
  );
}
