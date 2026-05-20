import React, { useState } from 'react'
import './MainLayout.css'
import { Outlet } from 'react-router-dom'
import { Header } from '../componets/header/Header'
import { Sidebar } from '../componets/sidebar/Sidebar'

const MainLayout = () => {
  const [sidebarOpen, setSidebarOpen] = useState(false)

  return (
    <div className="app-shell">
      <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />
      {sidebarOpen && (
        <div className="app-shell__backdrop" onClick={() => setSidebarOpen(false)} />
      )}
      <div className="app-shell__main">
        <Header onMenuClick={() => setSidebarOpen(true)} />
        <div className="app-shell__content">
          <Outlet />
        </div>
      </div>
    </div>
  )
}

export default MainLayout
