import React from 'react';
import { useNavigate } from 'react-router-dom';
import './resultView.css'; 
import useVehicleStore from '../../../store/useVehicleStore';

const ResultView = ({ title, subTitle, type ,buttonText = "처음으로", linkTo = "/" }) => {
  const navigate = useNavigate();
  const isSuccess=type==="success";
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
          <div className={`rv-icon-circle ${isSuccess ? 'rv-bg-success' : 'rv-bg-error'}`}>
            <svg className="rv-svg-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              {isSuccess ?
                <path d="M5 13l4 4L19 7" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round"/>
              :  <path d="M12 8v4M12 16h.01" stroke="white" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round"/>
              }
            </svg>
          </div>
        </div>

        <h1 className="rv-title">{title}</h1>
        <p className="rv-subtitle">{subTitle}</p>

        <div className="rv-line"></div>

        <div className="rv-btn-group">
          <button className="rv-action-btn rv-btn-home" onClick={homeHandler}>
            {buttonText}
          </button>
          
          {/* 실패했을 때만 "처음으로" 버튼을 하나 더 추가하고 싶다면 아래처럼 가능해요 */}
          {!isSuccess && <button className="rv-action-btn rv-btn-back" onClick={() => navigate("/selectedVehicle",{ replace: true })} >이전 화면으로</button>}
        </div>
      </div>
    </div>
  );
};

export default ResultView;