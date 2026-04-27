import React, { useState } from "react";
import axios from "axios"; // axios가 설치되어 있어야 해!
import api from "../auth/api/axios";

const ReportModal = ({ onClose }) => {
  // 1. 입력값 상태 관리
  const [carNumber, setCarNumber] = useState("");
  const [description, setDescription] = useState("");
  const [file, setFile] = useState(null);
  const [reportType, setReportType] = useState("ILLEGAL_PARKING");

  // 2. 접수 버튼 클릭 시 실행될 함수
  const handleSubmit = async () => {
    if (!carNumber || !description || !file) {
      alert("차량번호와 내용을 모두 입력해주세요!");
      return;
    }

    try{
     //파이썬 서버로 사진 먼저 보내기
     const pythonFormData = new FormData();
     pythonFormData.append("file",file);

     //파이썬 서버주소
     const pythonUrl = `${import.meta.env.VITE_AI_SERVER_URL}/api/v1/parking/report`

     console.log("파이썬을 사진 전송 중...");
     const pythonRes = await axios.post(pythonUrl, pythonFormData, {
        headers: { "Content-Type": "multipart/form-data"}
     });

     //파이썬 이 돌려준 s3 주소 꺼내기
     const s3Path = pythonRes.data.report_s3path;
     console.log("파이썬에서 받은 s3 주소:" ,s3Path);

     //자바 백엔드로 최종 데이터 보내기

     const params = new URLSearchParams();
     params.append("carNumber", carNumber);
     params.append("description", description);
     params.append("reportType", reportType); 
     params.append("report_s3path",s3Path); //파이썬이 준 주소를 자바에 전달

     await api.post('/api/report', params);
    
      alert("신고가 정상적으로 접수되었습니다!");
      onClose(); // 성공하면 모달 닫기
      window.location.reload(); // 데이터 갱신을 위해 새로고침 (또는 refetch)
    } catch (error) {
      console.error("신고 접수 에러:", error);
      console.error("서버 응답 데이터:", error.response?.data); 
  
        // test중
      alert(`에러 발생! 메시지: ${error.message}\n응답: ${JSON.stringify(error.response?.data)}`);

    //   alert("서버 전송에 실패했습니다. 백엔드 확인이 필요해요!");
    }
  };

  return (
    // 배경: 인라인 스타일로 확실하게 화면 중앙 고정!
    <div style={{
      position: "fixed", top: 0, left: 0, width: "100vw", height: "100vh",
      backgroundColor: "rgba(0, 0, 0, 0.5)", display: "flex",
      justifyContent: "center", alignItems: "center", zIndex: 9999
    }} onClick={onClose}>
      
      {/* 모달 박스 */}
      <div style={{
        backgroundColor: "white", padding: "30px", borderRadius: "16px",
        width: "90%", maxWidth: "400px", boxShadow: "0 10px 25px rgba(0,0,0,0.2)"
      }} onClick={(e) => e.stopPropagation()}>
        
        <h2 style={{ fontSize: "20px", fontWeight: "bold", marginBottom: "20px" }}>불법차량 신고</h2>

        <div style={{ display: "flex", flexDirection: "column", gap: "15px" }}>
          <input 
            type="text" placeholder="차량번호" 
            value={carNumber} onChange={(e) => setCarNumber(e.target.value)}
            style={{ width: "100%", padding: "10px", border: "1px solid #ddd", borderRadius: "8px" }}
          />

        <select value={reportType} onChange={(e) => setReportType(e.target.value)}
        style={{
          width:"100%",
          padding: "10px",
          border: "1px solid #ddd",
          borderRadius: "8px",
          backgroundColor: "white",
          fontSize: "14px",
          color: "#333",
          cursor: "pointer"
        }}>
          <option value="ILLEGAL_PARKING">🚨 일반 불법 주차</option>
          <option value="BLOCKING">🚧 통로 막음 (이동 불가)</option>
          <option value="DOUBLE_PARK">🅿️ 이중 주차</option>
          <option value="NOISE">🔊 소음 공해</option>
          <option value="OTHER">📝 기타 (상세내용 작성)</option>
        </select>

        <textarea 
            placeholder="신고 내용" 
            value={description} onChange={(e) => setDescription(e.target.value)}
            style={{ width: "100%", padding: "10px", border: "1px solid #ddd", borderRadius: "8px", height: "100px" }}
          />

          <div style={{ border: "2px dashed #ccc", padding: "15px", textAlign: "center", borderRadius: "8px" }}>
            <input 
              type="file" accept="image/*" 
              onChange={(e) => setFile(e.target.files[0])}
              style={{ marginBottom: "10px", width: "100%" }} 
            />
            <p style={{ fontSize: "12px", color: "#666" }}>차량 상태가 잘 보이게 찍어 주세요!!</p>
          </div>
        </div>

        <div style={{ marginTop: "20px", display: "flex", justifyContent: "flex-end", gap: "10px" }}>
          <button onClick={onClose} style={{ padding: "10px 20px", borderRadius: "8px", border: "1px solid #ddd", cursor: "pointer" }}>취소</button>
          <button 
            onClick={handleSubmit}
            style={{ padding: "10px 20px", borderRadius: "8px", border: "none", backgroundColor: "#3b82f6", color: "white", fontWeight: "bold", cursor: "pointer" }}
          >
            접수하기
          </button>
        </div>
      </div>
    </div>
  );
};

export default ReportModal;