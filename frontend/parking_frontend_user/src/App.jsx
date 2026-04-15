import React, { useEffect } from 'react';
import { BrowserRouter, Route, Routes, Navigate } from 'react-router-dom';

// 인증 라우트 컴포넌트
import { PublicRoute, PrivateRoute } from './features/auth/components/AuthRoute'; 

// 페이지 및 레이아웃 컴포넌트
import MainLayout from './shared/layouts/MainLayout.jsx';
import DashBoard from './features/dash/pages/DashBoard.jsx';
import LoginPage from './features/auth/pages/LoginPage.jsx';
import SignupPage from './features/auth/pages/SignupPage.jsx';
import OAuthRedirectPage from './features/auth/pages/OAuthRedirectPage.jsx';
import ReportPage from './features/report/ReportPage.jsx';
import MyPage from './features/mypage/pages/MyPage.jsx';
import ReservationPage from './features/reservation/pages/ReservationPage.jsx';

const PlaceholderPage = ({ title }) => (
  <div style={{ padding: '2rem' }}>
    <h2>{title}</h2>
    <p>준비 중입니다.</p>
  </div>
);

function App() {
  // [전역 청소 로직] 앱 진입 시 토큰 상태 점검
  useEffect(() => {
    const accessToken = localStorage.getItem("accessToken");
    const refreshToken = localStorage.getItem("refreshToken");

    // 토큰이 불완전하면 로그아웃 상태로 간주하고 정리
    if (!accessToken || !refreshToken) {
      if (localStorage.length > 0) {
        localStorage.clear();
        console.log("세션 정보가 불완전하여 스토리지를 정리했습니다.");
      }
    }
  }, []);

  return (
    <BrowserRouter>
      <Routes>
        {/* 1. 로그인: / 경로가 로그인 페이지임 */}
        <Route 
          path="/" 
          element={
            <PublicRoute>
              <LoginPage />
            </PublicRoute>
          } 
        />

        {/* 2. 회원가입 */}
        <Route 
          path="/signup" 
          element={
            <PublicRoute>
              <SignupPage />
            </PublicRoute>
          } 
        />

        {/* 소셜 로그인 리다이렉트 처리 */}
        <Route path="/oauth-redirect" element={<OAuthRedirectPage />} />

        {/* 3. 보호된 경로 (로그인 필요) */}
        <Route 
          element={
            <PrivateRoute>
              <MainLayout />
            </PrivateRoute>
          }
        >
          {/* 로그인 후 첫 화면은 대시보드 */}
          <Route path="/dashboard" element={<DashBoard />} />
          <Route path="/season-pass" element={<PlaceholderPage title="정기권" />} />
          <Route path="/visit" element={<ReservationPage />} />          
          <Route path="/mypage" element={<MyPage />} />
          <Route path="/complaints" element={<ReportPage title="민원/신고" />} />
        </Route>

        {/* 4. 잘못된 경로는 모두 루트(/)로 리다이렉트 */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;