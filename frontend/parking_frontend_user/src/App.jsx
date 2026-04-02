import { BrowserRouter, Route, Routes } from 'react-router-dom'
import MainLayout from './shared/layouts/MainLayout.jsx'
import DashBoard from './features/dash/pages/DashBoard.jsx'
import LoginPage from './features/auth/pages/LoginPage.jsx' // 1. 로그인 페이지 가져오기
import SignupPage from './features/auth/pages/SignupPage.jsx'
import OAuthRedirectPage from './features/auth/pages/OAuthRedirectPage.jsx'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* 2. 레이아웃이 없는 독립적인 경로 (첫 화면) */}
        <Route path="/" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route path="/oauth-redirect" element={<OAuthRedirectPage/>}></Route>

        {/* 3. 레이아웃이 필요한 경로들을 그룹화 */}
        <Route element={<MainLayout />}>
          <Route path="/dashboard" element={<DashBoard />} />
          
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App