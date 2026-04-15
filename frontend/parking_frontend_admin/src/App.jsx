import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom"
import Mainlayout from "./shared/layouts/Mainlayout"
import DashBoard from "./features/dashboard/pages/DashBoard"
import ParkingSpace from "./features/parkingspace/pages/ParkingSpacePage"
import LoginPage from "./features/auth/pages/LoginPage"
import ParkingLogPage from "./features/parking-management/pages/ParkingLogPage"


function App() {


  return (
    <BrowserRouter>
      <Routes>
        {/* 1. 레이아웃이 없는 독립 페이지(로그인) */}
        <Route path="/admin" element={<LoginPage />}/>

        {/* 2. 공통 레이아웃(헤어, 사이드바)이 적용되는 관리자 페이지들 */}
        <Route element={<Mainlayout />}>
          <Route path="/admin/dashboard" element={<DashBoard />} />
          <Route path="/admin/parking-space" element={<ParkingSpace />} />
          <Route path="/admin/entry-exit" element={<ParkingLogPage />} />
        </Route>

        {/* 예외 처리: 아무것도 없는"/"로 접속하거나 잘못된 경로일 때 */}
        <Route path="/" element={<Navigate to="/admin" replace />} />
        <Route path="*" element={<Navigate to="/admin" replace />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
