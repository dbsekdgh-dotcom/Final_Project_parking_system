import React, { useRef, useState } from "react";
import api from "../auth/api/axios";
import { alertValidation, alertReportSuccess, alertReportError } from "./components/ReportAlerts";

const REPORT_TYPES = [
  { value: "ILLEGAL_PARKING", label: "🚨 일반 불법 주차" },
  { value: "BLOCKING",        label: "🚧 통로 막음 (이동 불가)" },
  { value: "DOUBLE_PARK",     label: "🅿️ 이중 주차" },
  { value: "NOISE",           label: "🔊 소음 공해" },
  { value: "OTHER",           label: "📝 기타 (상세내용 작성)" },
];

export default function ReportModal({ onClose }) {
  const [carNumber, setCarNumber] = useState("");
  const [description, setDescription] = useState("");
  const [file, setFile] = useState(null);
  const [reportType, setReportType] = useState("ILLEGAL_PARKING");
  const [preview, setPreview] = useState(null);
  const [loading, setLoading] = useState(false);
  const fileInputRef = useRef(null);

  const handleFileChange = (e) => {
    const selected = e.target.files[0];
    if (!selected) return;
    setFile(selected);
    setPreview(URL.createObjectURL(selected));
  };

  const handleDrop = (e) => {
    e.preventDefault();
    const dropped = e.dataTransfer.files[0];
    if (!dropped) return;
    setFile(dropped);
    setPreview(URL.createObjectURL(dropped));
  };

  const handleSubmit = async () => {
    if (!carNumber.trim()) {
      await alertValidation("차량번호를 입력해 주세요.");
      return;
    }
    if (!description.trim()) {
      await alertValidation("신고 내용을 입력해 주세요.");
      return;
    }
    if (!file) {
      await alertValidation("차량 사진을 첨부해 주세요.");
      return;
    }

    setLoading(true);
    try {
      const pythonFormData = new FormData();
      pythonFormData.append("file", file);

      const pythonRes = await api.post(`/api/user/report/upload`, pythonFormData);

      const s3Path = pythonRes.data.report_s3path;

      const params = new URLSearchParams();
      params.append("carNumber", carNumber);
      params.append("description", description);
      params.append("reportType", reportType);
      params.append("report_s3path", s3Path);

      await api.post("/api/user/report", params);

      onClose();
      await alertReportSuccess();
      window.location.reload();
    } catch (error) {
      console.error("신고 접수 에러:", error);
      await alertReportError(error.response?.data?.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      position: "fixed", top: 0, left: 0, width: "100vw", height: "100vh",
      backgroundColor: "rgba(0,0,0,0.45)", display: "flex",
      justifyContent: "center", alignItems: "center", zIndex: 9999,
    }} onClick={onClose}>
      <div style={{
        backgroundColor: "#fff", borderRadius: "18px",
        width: "90%", maxWidth: "420px",
        boxShadow: "0 10px 24px rgba(86,95,132,0.15)",
        overflow: "hidden",
      }} onClick={(e) => e.stopPropagation()}>

        {/* 헤더 */}
        <div style={{
          background: "#cfe0ff", padding: "18px 24px",
          position: "relative", overflow: "hidden",
        }}>
          <h2 style={{ margin: 0, fontSize: "1.1rem", fontWeight: 700, color: "#233b6e", position: "relative", zIndex: 1 }}>
            불법차량 신고
          </h2>
          <div style={{
            position: "absolute", top: "-40px", right: "-60px",
            width: "220px", height: "160px", pointerEvents: "none",
            background: "radial-gradient(circle at top right, rgba(255,255,255,0.85) 0%, rgba(255,255,255,0.35) 35%, rgba(255,255,255,0) 70%)",
          }} />
        </div>

        {/* 본문 */}
        <div style={{ padding: "24px", display: "flex", flexDirection: "column", gap: "14px" }}>

        {/* 차량번호 */}
        <input
          type="text"
          placeholder="차량번호 (예: 12가3456)"
          value={carNumber}
          onChange={(e) => setCarNumber(e.target.value)}
          style={inputStyle}
        />

        {/* 신고 유형 */}
        <select
          value={reportType}
          onChange={(e) => setReportType(e.target.value)}
          style={inputStyle}
        >
          {REPORT_TYPES.map((t) => (
            <option key={t.value} value={t.value}>{t.label}</option>
          ))}
        </select>

        {/* 신고 내용 */}
        <textarea
          placeholder="신고 내용을 입력해 주세요."
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          style={{ ...inputStyle, height: "90px", resize: "none" }}
        />

        {/* 파일 업로드 */}
        <div
          onClick={() => fileInputRef.current?.click()}
          onDrop={handleDrop}
          onDragOver={(e) => e.preventDefault()}
          style={{
            border: `2px dashed ${file ? "#4f7af8" : "#d9ddef"}`,
            borderRadius: "10px",
            padding: "16px",
            textAlign: "center",
            cursor: "pointer",
            backgroundColor: file ? "#f5f5fa" : "#fafafa",
            transition: "all 0.2s",
          }}
        >
          {preview ? (
            <div>
              <img
                src={preview}
                alt="미리보기"
                style={{ maxHeight: "120px", maxWidth: "100%", borderRadius: "8px", objectFit: "cover" }}
              />
              <p style={{ margin: "8px 0 0", fontSize: "12px", color: "#6f708b" }}>
                {file.name}
              </p>
            </div>
          ) : (
            <div>
              <div style={{ fontSize: "28px", marginBottom: "6px" }}>📷</div>
              <p style={{ margin: 0, fontSize: "13px", color: "#6f708b", fontWeight: 600 }}>
                사진을 클릭하거나 드래그해서 첨부하세요
              </p>
              <p style={{ margin: "4px 0 0", fontSize: "11px", color: "#9e9eb8" }}>
                차량 번호판과 상태가 잘 보이게 찍어주세요
              </p>
            </div>
          )}
          <input
            ref={fileInputRef}
            type="file"
            accept="image/*"
            onChange={handleFileChange}
            style={{ display: "none" }}
          />
        </div>

        {/* 버튼 */}
        <div style={{ display: "flex", justifyContent: "center", gap: "10px", marginTop: "2px" }}>
          <button onClick={onClose} style={cancelBtnStyle}>취소</button>
          <button
            onClick={handleSubmit}
            disabled={loading}
            style={{ ...submitBtnStyle, opacity: loading ? 0.7 : 1 }}
          >
            {loading ? "접수 중..." : "접수하기"}
          </button>
        </div>

        </div>{/* 본문 끝 */}
      </div>
    </div>
  );
}

const inputStyle = {
  width: "100%",
  padding: "10px 12px",
  border: "1px solid #e7e6f2",
  borderRadius: "8px",
  fontSize: "14px",
  color: "#32324d",
  backgroundColor: "#fff",
  outline: "none",
  boxSizing: "border-box",
};

const cancelBtnStyle = {
  padding: "10px 20px",
  borderRadius: "10px",
  border: "1px solid #d9ddef",
  backgroundColor: "#f7f7fb",
  color: "#6f708b",
  fontSize: "0.88rem",
  fontWeight: 600,
  cursor: "pointer",
};

const submitBtnStyle = {
  padding: "10px 24px",
  borderRadius: "10px",
  border: "none",
  backgroundColor: "#cfe0ff",
  color: "#233b6e",
  fontSize: "0.88rem",
  fontWeight: 700,
  cursor: "pointer",
  boxShadow: "0 4px 12px rgba(86,95,132,0.12)",
};
