import { Sidebar } from './shared/componets/sidebar/Sidebar.jsx'
import { Header } from './shared/componets/header/Header.jsx'
import './App.css'
import { Outlet, Route, Routes } from 'react-router-dom'

function Shell() {
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

function App() {
  return (
    <Routes>
      <Route path="/" element={<Shell />}>
        <Route index element={null} />
        <Route path="season-pass" element={null} />
        <Route path="visit" element={null} />
        <Route path="mypage" element={null} />
        <Route path="complaints" element={null} />
      </Route>
    </Routes>
  )
}

export default App

