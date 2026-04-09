import React from 'react';
import { useNavigate } from 'react-router-dom';
import './SuccessView.css'; // 👈 CSS 파일 임포트

const SuccessView = ({ title, subTitle, buttonText = "처음으로", linkTo = "/" }) => {
  const navigate = useNavigate();

  return (
    <div className="success-container">
      <div className="success-content">
        <div className="check-icon-wrapper">
          <div className="check-icon-circle">
            <svg className="check-svg" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M5 13l4 4L19 7" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </div>
        </div>

        <h1 className="success-title">{title}</h1>
        <p className="success-subtitle">{subTitle}</p>

        <div className="success-divider"></div>

        <button className="home-button" onClick={() => navigate(linkTo)}>
          {buttonText}
        </button>
      </div>
    </div>
  );
};

export default SuccessView;