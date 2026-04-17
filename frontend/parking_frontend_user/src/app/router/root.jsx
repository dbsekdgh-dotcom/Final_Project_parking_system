import React from 'react'
import LoginPage from '../../features/auth/pages/LoginPage'
import SignupPage from '../../features/auth/pages/SignupPage'
import MainLayout from '../../shared/layouts/MainLayout'
import DashBoard from '../../features/dash/pages/DashBoard'
import MyPage from '../../features/mypage/pages/MyPage'
import ReservationPage from '../../features/reservation/pages/ReservationPage'
import ReportPage from '../../features/report/ReportPage'
import { createBrowserRouter } from 'react-router-dom'

const root = createBrowserRouter([
    {
        path: "/",
        element: <LoginPage />,
    },
    {
        path: "/signup",
        element: <SignupPage />,
    },
    {
        element: <MainLayout />,
        children: [
            {
                path: "/dashboard",
                element: <DashBoard />,
            },
            {
                path: "/mypage",
                element: <MyPage />,
            },
            {
                path: "/visit",
                element: <ReservationPage />,
            },
            {
                path: "/complaints",
                element: <ReportPage />,
            },
        ],
    },
])

export default root
