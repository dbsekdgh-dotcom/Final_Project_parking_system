import { BrowserRouter, Route, Routes } from "react-router-dom"
import Mainlayout from "./shared/layouts/Mainlayout"
import DashBoard from "./features/dashboard/pages/DashBoard"


function App() {
 

  return (
     <BrowserRouter>
      <Routes>
        {/* <Route path="/login" element={<LoginPage />} /> */}
        <Route element={<Mainlayout />}>
          <Route path="/dashboard" element={<DashBoard />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App
