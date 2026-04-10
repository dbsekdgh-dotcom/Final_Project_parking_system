import { Route, Routes, useNavigate } from "react-router-dom";
import Home from "./shared/components/home/Home";
import PrepaymentMain from "./features/prepayment/pages/PrepaymentMain";
import EntryExit from "./features/entryExit/pages/EntryExit";
import VehicleSearchResult from "./features/prepayment/pages/VehicleSearchResult";
import SelectedVehicleInfo from "./features/prepayment/pages/SelectedVehicleInfo";
import EntryParkingSpace from "./features/entry/entryparkingspace/pages/EntryParkingSpace";
import EntryCompletePage from "./features/entry/entrycomplate/pages/EntryComplatePage";
import VehicleDeparture from "./features/exit/pages/vehicledeparture/VehicleDeparture";
import PaymentConfirm from "./features/exit/pages/paymentconfirm/PaymentConfirm";
import DepartureComplete from "./features/exit/pages/departurecomplete/DepartureComplete";
import { PaymentPage } from "./shared/components/paymentPage/PaymentPage";
import {PaymentSuccessPage} from "./shared/components/paymentPage/PaymentSuccessPage";
import { PaymentFailPage } from "./shared/components/paymentPage/PaymentFailPage";
import PrepaymentResult from "./features/prepayment/pages/PrepaymentResult";


function App() {
  const navigate = useNavigate();
  return (
  <Routes >
      <Route path="/" element={<Home />}/>
      {/* 2. 각 버튼에 매칭되는 경로들 */}

      <Route path="/entry-exit" element={<EntryExit />} />
      <Route path="/entry-parkingspace" element={<EntryParkingSpace/>}/>
      <Route path="/entry-complete" element={<EntryCompletePage/>}/>
      <Route path="/prepayment" element={<PrepaymentMain/>}></Route>
      <Route path="/searchResult" element={<VehicleSearchResult />}></Route>
      <Route path="/selectedVehicle" element={<SelectedVehicleInfo />}></Route>
      <Route path="/exit-departure" element={<VehicleDeparture/>}/>
      <Route path="/exit-paymentconfirm" element={<PaymentConfirm/>}/>
      <Route path="/PrepaymentResult" element={<PrepaymentResult/>}></Route>
      {/* 결제 페이지 이동 */}
      <Route path="/payment" element={<PaymentPage/>}></Route>
      {/* 결제 성공/실패 화면*/}
      <Route path="/payment/success" element={<PaymentSuccessPage/>}></Route>
      <Route path="/payment/fail" element={<PaymentFailPage/>}></Route>
      <Route path="/exit-complete" element={<DepartureComplete onHome={() => navigate("/")}/>}/>
      <Route path="/store" element={<div style={{ padding: 40 }}>상가 관리 준비 중</div>} />
      <Route path="/find-car" element={<div style={{ padding: 40 }}>내차 찾기 준비 중</div>} />

  </Routes>
  );
}

export default App
