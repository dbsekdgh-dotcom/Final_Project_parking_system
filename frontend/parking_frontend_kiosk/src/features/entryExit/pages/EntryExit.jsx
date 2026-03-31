import React, { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./entryExit.css";

const OCR_ENDPOINT = "http://localhost:8000/api/v1/entry/plate-ocr";
const SLOT_COUNT = 7;

export default function EntryExit() {
  const navigate = useNavigate();

  const [previewUrl, setPreviewUrl] = useState(null);
  const [plateNumber, setPlateNumber] = useState("");
  const [ocrLoading, setOcrLoading] = useState(false);
  const [ocrError, setOcrError] = useState("");

  useEffect(() => {
    return () => {
      if (previewUrl) URL.revokeObjectURL(previewUrl);
    };
  }, [previewUrl]);

  const plateChars = useMemo(() => {
    const chars = Array.from(plateNumber);
    return Array.from({ length: SLOT_COUNT }, (_, i) => chars[i] ?? "");
  }, [plateNumber]);

  const handleFileChange = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (previewUrl) URL.revokeObjectURL(previewUrl);
    setPreviewUrl(URL.createObjectURL(file));

    setPlateNumber("");
    setOcrError("");
    setOcrLoading(true);

    try {
      const formData = new FormData();
      formData.append("file", file);

      const res = await fetch(OCR_ENDPOINT, {
        method: "POST",
        body: formData,
      });

      if (!res.ok) {
        throw new Error(`OCR 요청 실패 (${res.status})`);
      }

      const data = await res.json();

      const next =
        data?.plateNumber ?? data?.plate ?? data?.result ?? data?.licensePlate ?? "";

      if (typeof next !== "string" || next.trim().length === 0) {
        setOcrError("번호 인식 결과를 가져오지 못했습니다.");
        return;
      }

      setPlateNumber(next.replace(/\s+/g, ""));
    } catch {
      setOcrError("OCR 호출 실패(백엔드 엔드포인트 확인 필요)");
    } finally {
      setOcrLoading(false);
    }
  };

  return (
    <div className="entry-exit-page">
      <div className="entry-exit-topbar">
        <div className="entry-exit-title">입차 / 출차</div>
        <button className="back-button" type="button" onClick={() => navigate("/")}>
          돌아가기
        </button>
      </div>

      <div className="entry-exit-layout">
        <div className="plate-panel">
          <div className="plate-panel-label">차량 번호판</div>

          <div className="plate-underline">
            {plateChars.map((ch, idx) => (
              <div key={idx} className="plate-slot">
                {ch}
              </div>
            ))}
          </div>

          <label className="upload-button">
            <input type="file" accept="image/*" onChange={handleFileChange} />
            사진 업로드
          </label>

          <div className="upload-preview">
            {previewUrl ? (
              <img src={previewUrl} alt="업로드된 차량 사진" />
            ) : (
              <div className="upload-placeholder">사진이 여기에 표시됩니다</div>
            )}
          </div>

          {ocrLoading ? <div className="ocr-status">인식 중...</div> : null}
          {ocrError ? <div className="ocr-error">{ocrError}</div> : null}
        </div>

        <div className="action-panel">
          <button className="action-button" type="button">
            입차
          </button>
          <button className="action-button" type="button">
            출차
          </button>
        </div>
      </div>
    </div>
  );
}

