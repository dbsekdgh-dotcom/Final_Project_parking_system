import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom"
import Mainlayout from "./shared/layouts/Mainlayout"
import DashBoard from "./features/dashboard/pages/DashBoard"
import ParkingSpace from "./features/parkingspace/pages/ParkingSpacePage"
import LoginPage from "./features/auth/pages/LoginPage"
import ParkingLogPage from "./features/parking-management/pages/ParkingLogPage"
import Fee from "./features/fee/pages/Fee"
import SystemSettingPage from "./features/systemsetting/pages/SystemSettingStatusPage"
import ApprovalLayout from "./features/approval/layout/ApprovalLayout"
import ApprovalRequestPage from "./features/approval/approval-request/pages/ApprovalRequestPage"
import ReportPage from "./features/approval/report/pages/ReportPage"



function App() {


  return (
    <BrowserRouter>
      <Routes>
        {/* 1. 레이아웃이 없는 독립 페이지(로그인) */}
        <Route path="/admin" element={<LoginPage />}/>

        {/* 2. 공통 레이아웃(헤더, 사이드바)이 적용되는 관리자 페이지들 */}
        <Route element={<Mainlayout />}>
          <Route path="/admin/dashboard" element={<DashBoard />} />
          <Route path="/admin/parking-space" element={<ParkingSpace />} />
          <Route path="/admin/entry-exit" element={<ParkingLogPage />} />
          <Route path="/admin/fee" element={<Fee/>}/>
          <Route path="/admin/system-setting" element={<SystemSettingPage/>}/>

          {/* 승인 관리: 탭 레이아웃 + 하위 페이지 */}
          <Route path="/admin/approval" element={<ApprovalLayout />}>
            <Route path="approval-request" element={<ApprovalRequestPage />} />
            <Route path="report" element={<ReportPage />} />
          </Route>
        </Route>

        {/* 예외 처리: 아무것도 없는"/"로 접속하거나 잘못된 경로일 때 */}
        <Route path="/" element={<Navigate to="/admin" replace />} />
        <Route path="*" element={<Navigate to="/admin" replace />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
