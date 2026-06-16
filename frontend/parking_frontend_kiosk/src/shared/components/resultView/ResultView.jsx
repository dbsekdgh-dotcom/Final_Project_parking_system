import React from 'react';
import { useNavigate } from 'react-router-dom';
import './resultView.css'; 
import useVehicleStore from '../../../store/useVehicleStore';

const ResultView = ({ title, subTitle, type ,buttonText = "이전 화면으로", showBackButton = true}) => {
  const navigate = useNavigate();
  const isSuccess=type==="success";
  const isLoading = type === "loading";
  const {resetAll}=useVehicleStore();

  const homeHandler=()=>{
    sessionStorage.removeItem("pendingParkingLogId")
    sessionStorage.removeItem("pendingVehicleNumber")
    sessionStorage.removeItem("paymentFlow")
    resetAll()
    navigate("/",{ replace: true }) //히스토리 청소
  }

  return (
    <div className="rv-main-wrapper">
      <div className="rv-content-layout">
        <div className="rv-check-icon-wrapper">
          <div className={`rv-icon-circle ${isLoading ? 'rv-bg-loading' : isSuccess ? 'rv-bg-success' : 'rv-bg-error'}`}>
            <svg className={`rv-svg-icon ${isLoading ? 'rv-spin' : ''}`} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              {isSuccess ?
                <path d="M5 13l4 4L19 7" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round"/>
              : isLoading? <circle cx="12" cy="12" r="10" stroke="white" strokeWidth="3" strokeDasharray="31.4 31.4" strokeLinecap="round" />
                      : <path d="M12 8v4M12 16h.01" stroke="white" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round"/>
              }
            </svg>
          </div>
        </div>

        <h1 className="rv-title">{title}</h1>
        <p className="rv-subtitle">{subTitle}</p>
        {!isLoading && (
          <div className="rv-btn-group">
          <button className="rv-action-btn rv-btn-home" onClick={homeHandler}>처음으로</button>
          {!isSuccess && showBackButton && <button className="rv-action-btn rv-btn-back" onClick={() => navigate(-1)} >{buttonText}</button>}
          </div>
        )}     
      </div>
    </div>
  );
};

export default ResultView;