import { useState } from "react";
import "./PaymentConfirm.css";
import { useLocation, useNavigate } from "react-router-dom";
import { cancelExit } from "../../api/ExitApi";

export default function PaymentConfirm() {
  const { state } = useLocation();
  const navigate = useNavigate();
  const parkingLogId = state?.parkingLogId;
  const [loading, setLoading] = useState(false);

  const handleCancel = async () => {
    if (!parkingLogId) {
      navigate("/entry-exit");
      return;
    }
    setLoading(true);
    try {
      await cancelExit(parkingLogId);
      navigate("/entry-exit");
    } catch (err) {
      console.error("회차 실패", err);
      alert(err?.response?.data?.message || "취소 처리 실패");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="pc-backdrop">
      <div className="pc-modal">
        <div className="pc-icon-wrap">
          <svg
            className="pc-check-icon"
            viewBox="0 0 56 56"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
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

        <h2 className="pc-title">결제가 필요합니다.</h2>
        <p className="pc-subtitle">결제 진행을 누르면 결제를 진행합니다.</p>

        <div className="pc-divider" />

        <div className="pc-btn-group">
          <button className="pc-btn pc-btn--primary">
            결제 진행
          </button>
          <button className="pc-btn pc-btn--secondary" onClick={handleCancel} disabled={loading}>
            {loading ? "처리 중..." : "취소"}
          </button>
        </div>
      </div>
    </div>
  );
}
