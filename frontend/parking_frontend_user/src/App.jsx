import React from 'react';
import { BrowserRouter, Route, Routes, Navigate } from 'react-router-dom';

// 인증 라우트 컴포넌트
import { PublicRoute, PrivateRoute } from './features/auth/components/AuthRoute';

// 페이지 및 레이아웃 컴포넌트
import MainLayout from './shared/layouts/MainLayout.jsx';
import ChatButton from './shared/componets/chatbot/ChatButton.jsx';
import DashBoard from './features/dash/pages/DashBoard.jsx';
import LoginPage from './features/auth/pages/LoginPage.jsx';
import SignupPage from './features/auth/pages/SignupPage.jsx';
import OAuthRedirectPage from './features/auth/pages/OAuthRedirectPage.jsx';
import ReportPage from './features/report/ReportPage.jsx';
import MyPage from './features/mypage/pages/MyPage.jsx';
import ReservationPage from './features/reservation/pages/ReservationPage.jsx';
import SubscriptionPage from './features/subscription/pages/SubscriptionPage.jsx';
import SubscriptionSuccessPage from './features/subscription/pages/SubscriptionSuccessPage.jsx';
import SubscriptionFailPage from './features/subscription/pages/SubscriptionFailPage.jsx';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* 1. 공개 경로: 로그인, 회원가입 */}
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
        <Route path="/oauth-redirect" element={<OAuthRedirectPage />} />

        {/* 2. 보호된 경로 (로그인 필수) */}
        <Route
          element={
            <PrivateRoute>
              <>
                <MainLayout />
                <ChatButton />
              </>
            </PrivateRoute>
          }
        >
          {/* 하위 경로들: DashBoard에서 navigate로 보내는 주소와 일치시켜야 함 */}
          <Route path="/dashboard" element={<DashBoard />} />
          <Route path="/subscription" element={<SubscriptionPage title="정기권"/>} />
          <Route path="/subscription/success" element={<SubscriptionSuccessPage />} />
          <Route path="/subscription/fail" element={<SubscriptionFailPage />} />
          <Route path="/reservation" element={<ReservationPage title="방문예약" />} />
          <Route path="/mypage" element={<MyPage title="마이페이지" />} />
          <Route path="/report" element={<ReportPage title="민원/신고" />} />
        </Route>

        {/* 3. 잘못된 경로는 모두 루트(/)로 리다이렉트 */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;