import { Route, Routes } from "react-router-dom";
import Home from "./shared/components/home/Home";


function App() {

  return (
  <Routes >
      <Route path="/" element={<Home />} />
      {/* 2. 각 버튼에 매칭되는 경로들 */}

  </Routes>
  );
}

export default App
