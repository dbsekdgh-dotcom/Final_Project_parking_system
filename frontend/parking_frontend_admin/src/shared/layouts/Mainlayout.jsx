import React from 'react'
import Header from '../components/header/Header'
import Sidebar from '../components/sidebar/Sidebar'
import './Mainlayout.css'

const Mainlayout = () => {
  return (
    <div className="layout">
          <Sidebar />
          <div className="layout__main">
            <Header />
            <main className="layout__content" aria-label="콘텐츠 영역" />
          </div>
        </div>
  )
}

export default Mainlayout