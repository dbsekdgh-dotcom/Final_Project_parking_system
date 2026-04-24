import React from 'react'
import './MainLayout.css'
import { Outlet } from 'react-router-dom'
import { Header } from '../componets/header/Header'
import { Sidebar } from '../componets/sidebar/Sidebar'

const MainLayout = () => {
  return (
    <div className="app-shell">
      <Sidebar />
      <div className="app-shell__main">
        <Header />
        <Outlet />
      </div>
    </div>
  )
}

export default MainLayout
