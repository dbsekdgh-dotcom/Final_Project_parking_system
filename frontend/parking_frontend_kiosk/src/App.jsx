import { Route, Routes } from "react-router-dom";
import Home from "./shared/components/home/Home";
import PrepaymentMain from "./features/prepayment/pages/PrepaymentMain";
import EntryExit from "./features/entryExit/pages/EntryExit";
import VehicleSearchResult from "./features/prepayment/pages/VehicleSearchResult";
import SelectedVehicleInfo from "./features/prepayment/pages/SelectedVehicleInfo";



function App() {

  return (
  <Routes >
      <Route path="/" element={<Home />}/>
      {/* 2. 각 버튼에 매칭되는 경로들 */}

      <Route path="/entry-exit" element={<EntryExit />} />
      <Route path="/prepayment" element={<PrepaymentMain/>}></Route>
      <Route path="/searchResult" element={<VehicleSearchResult />}></Route>
      <Route path="/selectedVehicle" element={<SelectedVehicleInfo />}></Route>
      <Route path="/store" element={<div style={{ padding: 40 }}>상가 관리 준비 중</div>} />
      <Route path="/find-car" element={<div style={{ padding: 40 }}>내차 찾기 준비 중</div>} />

  </Routes>
  );
}

export default App
