import {  BrowserRouter, Route, Routes } from 'react-router-dom'
import MainLayout from './shared/layouts/MainLayout.jsx'
import DashBoard from './features/dash/DashBoard.jsx'



function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<MainLayout />}>
          <Route path='/dashboard' element={<DashBoard/>} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App

