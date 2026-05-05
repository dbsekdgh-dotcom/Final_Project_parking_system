import { Route, Routes, useNavigate, useLocation } from "react-router-dom";
import { useEffect } from "react";
import useChatbotStore from "./store/useChatbotStore";
import KioskChatbot from "./features/chatbot/components/KioskChatbot";
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
import { PaymentSuccessPage } from "./shared/components/paymentPage/PaymentSuccessPage";
import { PaymentFailPage } from "./shared/components/paymentPage/PaymentFailPage";
import PrepaymentResult from "./features/prepayment/pages/PrepaymentResult";
import StoreLoginPage from "./features/store/pages/StoreLoginPage";
import StoreMainPage from "./features/store/pages/StoreMainPage";
import StorePurchaseCompletePage from "./features/store/pages/StorePurchaseCompleatePage";
import TicketPurchasePage from "./features/store/pages/TicketPuchasePage";
import TicketApplyPage from "./features/store/pages/TicketApplyPage";
import FindCarPage from "./features/findcar/pages/FindCarPage";

const SCREEN_ID_BY_PATH = {
  "/": "home",

  // 내 차 찾기
  "/find-car": "find_input",

  // 사전 정산
  "/prepayment": "pay_input",
  "/searchResult": "pay_result",
  "/selectedVehicle": "pay_confirm",
  "/payment": "pay_method",

  // 결제 결과
  "/payment/success": "unknown",
  "/payment/fail": "unknown",

  // 상가 관리
  "/store/login": "store_login",
  "/store/main": "store_main",
  "/store/purchase": "store_buy_select",
  "/store/purchase/complete": "unknown",
  "/store/apply": "store_apply_input",

  // 출차/입차 쪽은  unknown 처리
  "/entry-exit": "unknown",
  "/entry-parkingspace": "unknown",
  "/entry-complete": "unknown",
  "/exit-departure": "unknown",
  "/exit-paymentconfirm": "unknown",
  "/exit-complete": "unknown",
  "/PrepaymentResult": "unknown",
};


function App() {
  const navigate = useNavigate();
  const location = useLocation();
  const setScreenId = useChatbotStore(s => s.setScreenId);

  useEffect(() => {
    setScreenId(SCREEN_ID_BY_PATH[location.pathname] || "unknown");
  }, [location.pathname]);

  return (
  <>
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
      <Route path="/store/login" element={<StoreLoginPage/>}/>
      <Route path="/store/main" element={<StoreMainPage/>}/>
      <Route path="/store/purchase" element={<TicketPurchasePage/>}/>
      <Route path="/store/purchase/complete" element={<StorePurchaseCompletePage/>}/>
      <Route path="/store/apply" element={<TicketApplyPage/>}/>
      <Route path="/find-car" element={<FindCarPage/>} />

  </Routes>
  {/* <KioskChatbot /> */}
  </>
  );
}

export default App
