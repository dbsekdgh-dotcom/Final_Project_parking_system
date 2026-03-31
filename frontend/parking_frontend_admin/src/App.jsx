import { BrowserRouter, Route, Routes } from "react-router-dom"
import Mainlayout from "./shared/layouts/Mainlayout"
import DashBoard from "./features/dashboard/pages/DashBoard"
import ParkingSpace from "./features/parkingspace/pages/ParkingSpace"


function App() {
 

  return (
     <BrowserRouter>
      <Routes>
        {/* <Route path="/admin" element={<LoginPage />}/> */}
    
        <Route element={<Mainlayout />}>
          <Route path="/admin/dashboard" element={<DashBoard />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App
