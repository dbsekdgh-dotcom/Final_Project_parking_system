import { useEffect } from 'react';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
// ⭐ 작성하신 인증 라우트 컴포넌트 임포트 (경로는 본인의 설정에 맞게 수정하세요)
import { PublicRoute, PrivateRoute } from './features/auth/components/AuthRoute'; 

import MainLayout from './shared/layouts/MainLayout.jsx';
import DashBoard from './features/dash/pages/DashBoard.jsx';
import LoginPage from './features/auth/pages/LoginPage.jsx';
import SignupPage from './features/auth/pages/SignupPage.jsx';
import OAuthRedirectPage from './features/auth/pages/OAuthRedirectPage.jsx';

function App() {
  // [전역 청소 로직] 앱 진입 시 토큰 없으면 스토리지 정리
  useEffect(() => {
    const accessToken = localStorage.getItem("accessToken");
    const refreshToken = localStorage.getItem("refreshToken");

    if (!accessToken || !refreshToken) {
      if (localStorage.length > 0) {
        localStorage.clear();
        console.log("세션 정보가 없어 스토리지를 정리했습니다.");
      }
    }
  }, []);

  return (
    <BrowserRouter>
      <Routes>
        {/* 1. 로그인/회원가입: 로그인한 사용자는 접근 불가 (PublicRoute) */}
        <Route 
          path="/" 
          element={
            <PublicRoute>
              <LoginPage />
            </PublicRoute>
          } 
        />
        <Route 
          path="/signup" 
          element={
            <PublicRoute>
              <SignupPage />
            </PublicRoute>
          } 
        />

        {/* 소셜 로그인 처리: 토큰을 받아오는 통로이므로 그대로 유지 */}
        <Route path="/oauth-redirect" element={<OAuthRedirectPage />} />

        {/* 2. 내부 서비스: 로그인 안 한 사용자는 접근 불가 (PrivateRoute) */}
        <Route 
          element={
            <PrivateRoute>
              <MainLayout />
            </PrivateRoute>
          }
        >
          {/* MainLayout 안에서 렌더링될 페이지들 */}
          <Route path="/dashboard" element={<DashBoard />} />
          
          {/* 나중에 추가될 회원탈퇴, 마이페이지 등도 이 안(PrivateRoute)에 넣으시면 됩니다! */}
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;