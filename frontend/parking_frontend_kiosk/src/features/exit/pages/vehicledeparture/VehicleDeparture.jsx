import { useState } from "react";
import "./VehicleDeparture.css";
import { useLocation, useNavigate } from "react-router-dom";
import { cancelExit, confirmExit } from "../../api/ExitApi";

export default function VehicleDeparture() {
  const { state } = useLocation();
  const navigate = useNavigate();
  const parkingLogId = state?.parkingLogId;
  const message= state?.message;

  const [loading,setLoading] = useState(false);

  //출차 확정 handler
  const handleDepart = async()=>{
    // parkingLogId가 넘어오지 못했을경우 
    if (!parkingLogId){
      alert("차량 정보를 확인 할 수 없습니다")
      return;
    }
    setLoading(true);
    try{
      await confirmExit(parkingLogId);
      navigate("/exit-complete");
    }catch (err){
      console.error("출차 실패", err);
      alert(err?.response?.data?.message || "출차 처리 실패");
    }finally{
      setLoading(false);
    }
  };
  //회차 handler
  const handleCancel = async ()=>{
    if(!parkingLogId){
      navigate("/entry-exit");
      return;
    }
    setLoading(true);
    try{
      await cancelExit(parkingLogId);
      navigate("/entry-exit");
    }catch(err){
      console.error("회차 실패:",err);
      alert(err?.response?.data?.message || "회차 처리 실패")
    }finally{
      setLoading(false)
    }
  };

  return (
    <div className="vd-backdrop">
      <div className="vd-modal">
        <div className="vd-icon-wrap">
          <svg
            className="vd-check-icon"
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

        <h2 className="vd-title">출차를 진행하시겠습니까?</h2>
        <p className="vd-subtitle">출차 버튼을 누르면 일시가 기록됩니다</p>

        <div className="vd-divider" />

        <div className="vd-btn-group">
          <button className="vd-btn vd-btn--primary" onClick={handleDepart} disabled={loading}>
            {loading ? "처리 중..." : "출차"}
          </button>
          <button className="vd-btn vd-btn--secondary" onClick={handleCancel} disabled={loading}>
            회차
          </button>
        </div>
      </div>
    </div>
  );
}
