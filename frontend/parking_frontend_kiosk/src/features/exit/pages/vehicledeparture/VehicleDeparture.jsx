import { useState } from "react";
import "./VehicleDeparture.css";

export default function VehicleDeparture() {
  const [status, setStatus] = useState(null); // null | 'confirmed' | 'cancelled'

  const handleDepart = () => setStatus("confirmed");
  const handleCancel = () => setStatus("cancelled");
  const handleReset = () => setStatus(null);

  return (
    <div className="vd-backdrop">
      <div className="vd-modal">
        {status === null && (
          <>
            <div className="vd-icon-wrap">
              <svg
                className="vd-check-icon"
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

            <h2 className="vd-title">출차를 진행하시겠습니까?</h2>
            <p className="vd-subtitle">출차 버튼을 누르면 일시가 기록됩니다</p>

            <div className="vd-divider" />

            <div className="vd-btn-group">
              <button className="vd-btn vd-btn--primary" onClick={handleDepart}>
                출차
              </button>
              <button className="vd-btn vd-btn--secondary" onClick={handleCancel}>
                회차
              </button>
            </div>
          </>
        )}

        {status === "confirmed" && (
          <div className="vd-result vd-result--success">
            <div className="vd-result-icon">
              <svg viewBox="0 0 56 56" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="28" cy="28" r="26" stroke="currentColor" strokeWidth="2.5" />
                <path
                  d="M17 28.5L24.5 36L39 21"
                  stroke="currentColor"
                  strokeWidth="2.5"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                />
              </svg>
            </div>
            <p className="vd-result-text">출차가 기록되었습니다</p>
            <button className="vd-btn vd-btn--ghost" onClick={handleReset}>
              닫기
            </button>
          </div>
        )}

        {status === "cancelled" && (
          <div className="vd-result vd-result--neutral">
            <div className="vd-result-icon">
              <svg viewBox="0 0 56 56" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="28" cy="28" r="26" stroke="currentColor" strokeWidth="2.5" />
                <path
                  d="M28 18v12M28 36v2"
                  stroke="currentColor"
                  strokeWidth="2.5"
                  strokeLinecap="round"
                />
              </svg>
            </div>
            <p className="vd-result-text">취소되었습니다</p>
            <button className="vd-btn vd-btn--ghost" onClick={handleReset}>
              돌아가기
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
