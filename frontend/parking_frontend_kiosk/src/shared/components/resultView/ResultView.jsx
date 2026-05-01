import React from 'react';
import { useNavigate } from 'react-router-dom';
import './resultView.css'; 
import useVehicleStore from '../../../store/useVehicleStore';

const ResultView = ({ title, subTitle, type ,buttonText = "이전 화면으로"}) => {
  const navigate = useNavigate();
  const isSuccess=type==="success";
  const isLoading = type === "loading";
  const {resetAll}=useVehicleStore();

  const homeHandler=()=>{
    localStorage.removeItem("pendingParkingLogId")
    resetAll()
    navigate("/",{ replace: true }) //히스토리 청소
  }

  return (
    <div className="rv-main-wrapper">
      <div className="rv-content-layout">
        <div className="rv-check-icon-wrapper">
          <div className={`rv-icon-circle ${isLoading ? 'rv-bg-loading' : isSuccess ? 'rv-bg-success' : 'rv-bg-error'}`}>
            <svg className="rv-svg-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              {isSuccess ?
                <path d="M5 13l4 4L19 7" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round"/>
              : isLoading? <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83" 
                      stroke="white" strokeWidth="2" strokeLinecap="round"/>
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
          {!isSuccess && <button className="rv-action-btn rv-btn-back" onClick={() => navigate(-1)} >{buttonText}</button>}
          </div>
        )}     
      </div>
    </div>
  );
};

export default ResultView;