import React, { useEffect, useMemo, useState } from "react";
import api from "../api/axios";
import "./AuthAccountLinkWidget.css";
import "./SocialLoginButtons.css";

import kakaoIcon from "../../../assets/images/kakao_login_icon.png";
import naverIcon from "../../../assets/images/naver_login_icon.png";
import localIcon from "../../../assets/images/local_login_icon.png";

function LocalIcon() {
  return (
    <img
      className="aalw__icon"
      src={localIcon}
      alt="로컬 로그인"
      aria-label="로컬 로그인"
    />
  );
}

function KakaoIcon() {
  return (
    <img
      className="aalw__icon"
      src={kakaoIcon}
      alt="카카오 로그인"
      aria-label="카카오 로그인"
    />
  );
}

function NaverIcon() {
  return (
    <img
      className="aalw__icon"
      src={naverIcon}
      alt="네이버 로그인"
      aria-label="네이버 로그인"
    />
  );
}

export default function AuthAccountLinkWidget({ metaText }) {
  const [me, setMe] = useState({
    name: "",
    email: "",
    phone: "",
    birth: "",
    hasLocalPassword: false,
    hasKakao: false,
    hasNaver: false,
  });
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [showLocalForm, setShowLocalForm] = useState(false);

  const [localPassword, setLocalPassword] = useState("");
  const [localPasswordConfirm, setLocalPasswordConfirm] = useState("");
  const [localError, setLocalError] = useState("");

  const hasLocalPassword = me.hasLocalPassword;
  const hasKakao = me.hasKakao;
  const hasNaver = me.hasNaver;

  const allLinked = useMemo(
    () => hasLocalPassword && hasKakao && hasNaver,
    [hasLocalPassword, hasKakao, hasNaver]
  );

  const shouldShowAccountLink = !isLoading && !allLinked;
  const shouldShowLocalLinkFormInModal = !hasLocalPassword;

  const fetchMe = async () => {
    try {
      setIsLoading(true);
      const token = localStorage.getItem("accessToken");
      if (!token) {
        setMe({
          name: "",
          hasLocalPassword: false,
          hasKakao: false,
          hasNaver: false,
        });
        return;
      }

      const res = await api.get("/api/user/auth/local/me");
      setMe(res.data);
    } catch (e) {
      // 네트워크/인증 문제 시, 최소 UI만 남기기
      console.error("계정 상태 조회 실패:", e);
      setMe((prev) => ({
        ...prev,
        name: prev.name || localStorage.getItem("userName") || "사용자",
      }));
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchMe();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const closeModal = () => {
    setIsModalOpen(false);
    setShowLocalForm(false);
    setLocalPassword("");
    setLocalPasswordConfirm("");
    setLocalError("");
  };

  const handleLinkLocal = async (e) => {
    e.preventDefault();
    setLocalError("");

    if (!localPassword || !localPasswordConfirm) {
      setLocalError("비밀번호를 입력해주세요.");
      return;
    }
    if (localPassword !== localPasswordConfirm) {
      setLocalError("비밀번호가 일치하지 않습니다.");
      return;
    }

    try {
      await api.post("/api/user/auth/local/link-password", {
        password: localPassword,
        passwordConfirm: localPasswordConfirm,
      });
      closeModal();
      await fetchMe();
    } catch (err) {
      const serverMessage =
        err?.response?.data?.message || "로컬 계정 연동에 실패했습니다.";
      setLocalError(serverMessage);
    }
  };

  const handleLinkSocial = (provider) => {
    closeModal();
    const base = "http://localhost:8081/oauth2/authorization";
    window.location.href = `${base}/${provider}`;
  };

  const userName = me.name || localStorage.getItem("userName") || "사용자";

  return (
    <div className="aalw">
      <div className="aalw__name-row">
        <div className="sidebar__user-name">{isLoading ? "..." : userName}</div>
        <div className="aalw__icons" aria-label="로그인 연동 상태">
          {hasLocalPassword ? <LocalIcon /> : null}
          {hasKakao ? <KakaoIcon /> : null}
          {hasNaver ? <NaverIcon /> : null}
        </div>
      </div>

      <div className="sidebar__user-meta">{metaText}</div>

      {shouldShowAccountLink ? (
        <button type="button" className="aalw__account-link-btn" onClick={() => setIsModalOpen(true)}>
          계정연동
        </button>
      ) : null}

      {isModalOpen ? (
        <div
          className="aalw__modal-overlay"
          role="dialog"
          aria-modal="true"
          onMouseDown={(e) => {
            if (e.target === e.currentTarget) closeModal();
          }}
        >
          <div className="aalw__modal">
            <div className="aalw__modal-header">
              <div className="aalw__modal-title">계정 연동</div>
              <button type="button" className="aalw__modal-close" onClick={closeModal} aria-label="닫기">
                ✕
              </button>
            </div>

            <div className="aalw__modal-body">
              {shouldShowLocalLinkFormInModal ? (
                showLocalForm ? (
                  <form className="aalw__local-form" onSubmit={handleLinkLocal}>
                    <div className="aalw__field">
                      <label className="aalw__label">이름</label>
                      <input className="aalw__input" value={me.name} disabled readOnly />
                    </div>
                    <div className="aalw__field">
                      <label className="aalw__label">이메일</label>
                      <input className="aalw__input" value={me.email} disabled readOnly />
                    </div>
                    <div className="aalw__field">
                      <label className="aalw__label">생년월일</label>
                      <input className="aalw__input" value={me.birth ?? ""} disabled readOnly />
                    </div>
                    <div className="aalw__field">
                      <label className="aalw__label">전화번호</label>
                      <input className="aalw__input" value={me.phone} disabled readOnly />
                    </div>
                    <div className="aalw__field">
                      <label className="aalw__label" htmlFor="aalw_local_password">비밀번호</label>
                      <input
                        id="aalw_local_password"
                        type="password"
                        placeholder="비밀번호"
                        className="aalw__input"
                        value={localPassword}
                        onChange={(e) => setLocalPassword(e.target.value)}
                      />
                    </div>
                    <div className="aalw__field">
                      <label className="aalw__label" htmlFor="aalw_local_password_confirm">비밀번호 확인</label>
                      <input
                        id="aalw_local_password_confirm"
                        type="password"
                        placeholder="비밀번호 확인"
                        className="aalw__input"
                        value={localPasswordConfirm}
                        onChange={(e) => setLocalPasswordConfirm(e.target.value)}
                      />
                    </div>
                    {localError ? <div className="aalw__error">{localError}</div> : null}
                    <button type="submit" className="aalw__primary-btn">
                      <img className="socialLoginButton__icon" src={localIcon} alt="" aria-hidden />
                      로컬 연동하기
                    </button>
                  </form>
                ) : (
                  <button
                    type="button"
                    className="socialLoginButton"
                    style={{ background: "#f1f5f9", color: "#334155", border: "1px solid rgba(148,163,184,0.4)" }}
                    onClick={() => setShowLocalForm(true)}
                  >
                    <img className="socialLoginButton__icon" src={localIcon} alt="" aria-hidden />
                    로컬 연동하기
                  </button>
                )
              ) : null}

              {!showLocalForm && <div className="aalw__social-buttons">
                {!hasNaver ? (
                  <button
                    type="button"
                    className="socialLoginButton socialLoginButtonNaver"
                    onClick={() => handleLinkSocial("naver")}
                  >
                    <img className="socialLoginButton__icon" src={naverIcon} alt="" aria-hidden />
                    네이버 연동하기
                  </button>
                ) : null}

                {!hasKakao ? (
                  <button
                    type="button"
                    className="socialLoginButton socialLoginButtonKakao"
                    onClick={() => handleLinkSocial("kakao")}
                  >
                    <img className="socialLoginButton__icon" src={kakaoIcon} alt="" aria-hidden />
                    카카오 연동하기
                  </button>
                ) : null}
              </div>}
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}

