import React, { useEffect, useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom"; // useNavigate 추가
import api from "../api/axios";
import "./auth-account-link-widget.css";
import "./SocialLoginButtons.css";

import kakaoIcon from "../../../assets/images/kakao_login_icon.png";
import naverIcon from "../../../assets/images/naver_login_icon.png";
import localIcon from "../../../assets/images/local_login_icon.png";

// --- 부속 아이콘 컴포넌트들 ---
function LocalIcon() {
  return <img className="aalw__icon" src={localIcon} alt="로컬" />;
}
function KakaoIcon() {
  return <img className="aalw__icon" src={kakaoIcon} alt="카카오" />;
}
function NaverIcon() {
  return <img className="aalw__icon" src={naverIcon} alt="네이버" />;
}

export default function AuthAccountLinkWidget({ metaText }) {
  const queryClient = useQueryClient();
  const navigate = useNavigate(); // 리다이렉트를 위한 훅

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isLocalFormModalOpen, setIsLocalFormModalOpen] = useState(false);
  const [localPassword, setLocalPassword] = useState("");
  const [localPasswordConfirm, setLocalPasswordConfirm] = useState("");
  const [localError, setLocalError] = useState("");

  const { data: me, isLoading } = useQuery({
    queryKey: ["me"],
    queryFn: async () => {
      const res = await api.get("/api/user/auth/local/me");
      return res.data;
    },
    throwOnError: false,
    retry: 1,
  });

  const { 
    hasLocalPassword, 
    hasKakao, 
    hasNaver, 
    name: userName, 
    email: userEmail 
  } = me || {};

  const shouldShowAccountLink = !isLoading && !(hasLocalPassword && hasKakao && hasNaver);

  const linkLocalMutation = useMutation({
    mutationFn: (newPasswordData) => api.post("/api/user/auth/local/link-password", newPasswordData),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["me"] });
      alert("로컬 계정 연동에 성공했습니다.");
      closeLocalFormModal();
    },
    onError: (error) => {
      const msg = error?.response?.data?.message || "연동 중 오류가 발생했습니다.";
      setLocalError(msg);
    }
  });

  // ---------------------------------------------------------
  // [소셜 연동 처리] 쿠키 저장 후 백엔드로 이동
  // ---------------------------------------------------------
  const handleLinkSocial = (provider) => {
    const accessToken = localStorage.getItem("accessToken");

    if (accessToken) {
      // 5분 동안 유지되는 임시 쿠키 생성 (백엔드 validateUserMatchingWithCookie에서 읽음)
      document.cookie = `temp_jwt=${accessToken}; path=/; max-age=300; SameSite=Lax`;
    }

    setIsModalOpen(false);
    // 백엔드 OAuth 엔드포인트로 이동
    const base = "http://localhost:8080/oauth2/authorization";
    window.location.href = `${base}/${provider}`;
  };

  const handleLinkLocalSubmit = (e) => {
    e.preventDefault();
    setLocalError("");

    if (localPassword.length < 8) {
      setLocalError("비밀번호는 최소 8자리 이상이어야 합니다.");
      return;
    }
    if (localPassword !== localPasswordConfirm) {
      setLocalError("비밀번호가 일치하지 않습니다.");
      return;
    }

    linkLocalMutation.mutate({
      password: localPassword,
      passwordConfirm: localPasswordConfirm,
    });
  };

  const closeModal = () => { setIsModalOpen(false); setLocalError(""); };
  const closeLocalFormModal = () => { 
    setIsLocalFormModalOpen(false); 
    setLocalPassword(""); 
    setLocalPasswordConfirm("");
    setLocalError(""); 
  };
  const openLocalFormModal = () => { setIsModalOpen(false); setIsLocalFormModalOpen(true); };

  // ---------------------------------------------------------
  // [에러 감지] URL 파라미터에 error=email_mismatch가 있을 때 처리
  // ---------------------------------------------------------
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const error = params.get("error");

    if (error === "email_mismatch") {
      alert("현재 로그인된 계정 정보와 일치하는 소셜 계정만 연동할 수 있습니다.");
      // URL에서 에러 파라미터 제거 (지저분하지 않게)
      navigate(window.location.pathname, { replace: true });
    } else if (error) {
      alert("연동 처리 중 오류가 발생했습니다.");
      navigate(window.location.pathname, { replace: true });
    }
  }, [navigate]);

  return (
    <div className="aalw">
      <div className="aalw__name-row">
        <div className="sidebar__user-name">{isLoading ? "..." : (userName || "사용자")}</div>
        <div className="aalw__icons" aria-label="로그인 연동 상태">
          {hasLocalPassword && <LocalIcon />}
          {hasKakao && <KakaoIcon />}
          {hasNaver && <NaverIcon />}
        </div>
      </div>

      <div className="sidebar__user-meta">{metaText}</div>

      {shouldShowAccountLink && (
        <button type="button" className="aalw__account-link-btn" onClick={() => setIsModalOpen(true)}>
          계정연동
        </button>
      )}

      {/* 메인 선택 모달 */}
      {isModalOpen && (
        <div className="aalw__modal-overlay" onMouseDown={(e) => e.target === e.currentTarget && closeModal()}>
          <div className="aalw__modal">
            <div className="aalw__modal-header">
              <div className="aalw__modal-title">계정 연동</div>
              <button className="aalw__modal-close" onClick={closeModal}>✕</button>
            </div>
            <div className="aalw__modal-body">
              <div className="aalw__social-buttons">
                {!hasNaver && (
                  <button className="socialLoginButton socialLoginButtonNaver" onClick={() => handleLinkSocial("naver")}>
                    <img className="socialLoginButton__icon" src={naverIcon} alt="" /> 네이버 연동하기
                  </button>
                )}
                {!hasKakao && (
                  <button className="socialLoginButton socialLoginButtonKakao" onClick={() => handleLinkSocial("kakao")}>
                    <img className="socialLoginButton__icon" src={kakaoIcon} alt="" /> 카카오 연동하기
                  </button>
                )}
              </div>
              {!hasLocalPassword && (
                <button className="aalw__local-link-btn" onClick={openLocalFormModal}>
                  <img className="aalw__local-link-icon" src={localIcon} alt="" /> 로컬 계정 연동하기
                </button>
              )}
            </div>
          </div>
        </div>
      )}

      {/* 로컬 비밀번호 입력 모달 */}
      {isLocalFormModalOpen && (
        <div className="aalw__modal-overlay" onMouseDown={(e) => e.target === e.currentTarget && closeLocalFormModal()}>
          <div className="aalw__modal">
            <div className="aalw__modal-header">
              <div className="aalw__modal-title">로컬 계정 연동</div>
              <button className="aalw__modal-close" onClick={closeLocalFormModal}>✕</button>
            </div>
            <div className="aalw__modal-body">
              <div className="aalw__readonly-grid">
                <div className="aalw__field">
                  <label className="aalw__label">이름</label>
                  <input className="aalw__input" value={userName || ""} disabled />
                </div>
                <div className="aalw__field">
                  <label className="aalw__label">이메일</label>
                  <input className="aalw__input" value={userEmail || ""} disabled />
                </div>
              </div>

              <form className="aalw__local-form" onSubmit={handleLinkLocalSubmit}>
                <div className="aalw__field">
                  <label className="aalw__label">비밀번호 (8자 이상)</label>
                  <input 
                    type="password" 
                    className="aalw__input" 
                    value={localPassword} 
                    onChange={(e) => setLocalPassword(e.target.value)} 
                    required 
                  />
                </div>
                <div className="aalw__field">
                  <label className="aalw__label">비밀번호 확인</label>
                  <input 
                    type="password" 
                    className="aalw__input" 
                    value={localPasswordConfirm} 
                    onChange={(e) => setLocalPasswordConfirm(e.target.value)} 
                    required 
                  />
                </div>
                {localError && <div className="aalw__error">{localError}</div>}
                <button type="submit" className="aalw__primary-btn" disabled={linkLocalMutation.isPending}>
                  {linkLocalMutation.isPending ? "연동 중..." : "로컬 계정 연동"}
                </button>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}