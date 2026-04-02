import React from 'react'
import LoginPage from '../../features/auth/pages/LoginPage'
import SignupPage from '../../features/auth/pages/SignupPage'
import MainLayout from '../../shared/layouts/MainLayout'
import DashBoard from '../../features/dash/DashBoard'
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
        path: "/main",
        element: <MainLayout />,
        children: [
            {
                index: true,
                element: <DashBoard />,
            },
            {
                path: "dashboard",
                element: <DashBoard />,
            },
        ],
    },
])

    

export default root