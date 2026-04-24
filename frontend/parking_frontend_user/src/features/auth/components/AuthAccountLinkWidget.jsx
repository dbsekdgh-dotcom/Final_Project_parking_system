import React, { useEffect, useMemo, useState } from "react";
import api from "../api/axios";
import "./AuthAccountLinkWidget.css";
import PasswordChangeButton from "./PasswordChangeButton";
import { openLinkLocalPasswordModal } from "../utils/accountUtils.js"; 
import kakaoIcon from "../../../assets/images/kakao_login_icon.png";
import naverIcon from "../../../assets/images/naver_login_icon.png";
import localIcon from "../../../assets/images/local_login_icon.png";

const LocalIcon = () => <img className="aalw__icon" src={localIcon} alt="로컬" />;
const KakaoIcon = () => <img className="aalw__icon" src={kakaoIcon} alt="카카오" />;
const NaverIcon = () => <img className="aalw__icon" src={naverIcon} alt="네이버" />;

export default function AuthAccountLinkWidget({ metaText, badge }) {
  const [me, setMe] = useState({
    name: "", email: "", phone: "", 
    hasLocalPassword: false, hasKakao: false, hasNaver: false,
  });
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);

  const fetchMe = async () => {
    try {
      setIsLoading(true);
      const res = await api.get("/api/user/auth/local/me");
      setMe(res.data);
      // 이름이 변경되었을 수 있으므로 로컬스토리지 동기화
      if (res.data.name) localStorage.setItem("userName", res.data.name);
    } catch (e) {
      console.error("내 정보 불러오기 실패");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => { fetchMe(); }, []);

  const allLinked = useMemo(() => me.hasLocalPassword && me.hasKakao && me.hasNaver, [me]);
  const userName = me.name || localStorage.getItem("userName") || "사용자";

  const handleLinkLocalClick = () => {
    setIsModalOpen(false);
    openLinkLocalPasswordModal(fetchMe);
  };

  const handleLinkSocial = (provider) => {
    window.location.href = `/oauth2/authorization/${provider}`;
  };

  return (
    <div className="aalw">
      <div className="sidebar__user-name">{isLoading ? "..." : userName}</div>

      {badge && (
        <span className={`sidebar__badge ${badge.cls}`}>{badge.label}</span>
      )}

      <div className="aalw__icons">
        {me.hasLocalPassword && <LocalIcon />}
        {me.hasKakao && <KakaoIcon />}
        {me.hasNaver && <NaverIcon />}
      </div>

      {metaText && (
        <div className="sidebar__user-meta">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
            <polyline points="9 22 9 12 15 12 15 22"/>
          </svg>
          {metaText}
        </div>
      )}

      <div className="aalw__btn-row">
        {!isLoading && !allLinked && (
          <button type="button" className="aalw__account-link-btn" onClick={() => setIsModalOpen(true)}>
            계정연동
          </button>
        )}
        {me.hasLocalPassword && <PasswordChangeButton name={me.name} email={me.email} phone={me.phone} />}
      </div>

      {isModalOpen && (
        <div className="aalw__modal-overlay" onMouseDown={(e) => e.target === e.currentTarget && setIsModalOpen(false)}>
          <div className="aalw__modal">
            <div className="aalw__modal-header">
              <div className="aalw__modal-title">계정 연동</div>
              <button type="button" className="aalw__modal-close" onClick={() => setIsModalOpen(false)}>✕</button>
            </div>
            <div className="aalw__modal-body">
              <div className="aalw__social-buttons">
                {!me.hasLocalPassword && (
                  <button type="button" className="socialLoginButton" onClick={handleLinkLocalClick} style={{ border: "1px solid #e2e8f0" }}>
                    <img className="socialLoginButton__icon" src={localIcon} alt="" /> 로컬 연동
                  </button>
                )}
                {!me.hasNaver && (
                  <button type="button" className="socialLoginButton socialLoginButtonNaver" onClick={() => handleLinkSocial("naver")}>
                    <img className="socialLoginButton__icon" src={naverIcon} alt="" /> 네이버 연동
                  </button>
                )}
                {!me.hasKakao && (
                  <button type="button" className="socialLoginButton socialLoginButtonKakao" onClick={() => handleLinkSocial("kakao")}>
                    <img className="socialLoginButton__icon" src={kakaoIcon} alt="" /> 카카오 연동
                  </button>
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}