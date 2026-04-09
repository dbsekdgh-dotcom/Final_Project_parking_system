import { useState } from "react";
import "./PaymentConfirm.css";

export default function PaymentConfirm() {
  const [status, setStatus] = useState(null); // null | 'success' | 'cancelled'

  const handlePay = () => setStatus("success");
  const handleCancel = () => setStatus("cancelled");
  const handleReset = () => setStatus(null);

  return (
    <div className="pc-backdrop">
      <div className="pc-modal">
        {status === null && (
          <>
            <div className="pc-icon-wrap">
              <svg
                className="pc-check-icon"
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

            <h2 className="pc-title">결제가 필요합니다.</h2>
            <p className="pc-subtitle">결제 진행을 누르면 결제를 진행합니다.</p>

            <div className="pc-divider" />

            <div className="pc-btn-group">
              <button className="pc-btn pc-btn--primary" onClick={handlePay}>
                결제 진행
              </button>
              <button className="pc-btn pc-btn--secondary" onClick={handleCancel}>
                취소
              </button>
            </div>
          </>
        )}

        {status === "success" && (
          <div className="pc-result pc-result--success">
            <div className="pc-result-icon">
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
            <p className="pc-result-text">결제가 완료되었습니다</p>
            <button className="pc-btn pc-btn--ghost" onClick={handleReset}>
              닫기
            </button>
          </div>
        )}

        {status === "cancelled" && (
          <div className="pc-result pc-result--neutral">
            <div className="pc-result-icon">
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
            <p className="pc-result-text">결제가 취소되었습니다</p>
            <button className="pc-btn pc-btn--ghost" onClick={handleReset}>
              돌아가기
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
