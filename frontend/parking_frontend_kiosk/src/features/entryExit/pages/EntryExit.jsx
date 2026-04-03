import React, { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./entryExit.css";
import { usePlateOCRMutation } from "../../entry/hooks/usePlateImage";
import { useQueryClient } from "@tanstack/react-query";

const SLOT_COUNT = 8;

export default function EntryExit() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const { mutate, isPending, isError } = usePlateOCRMutation();

  const [previewUrl, setPreviewUrl] = useState(null);
  const [plateNumber, setPlateNumber] = useState("");
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

  // ✅ OCR 실행
  const handleFileChange = (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (previewUrl) URL.revokeObjectURL(previewUrl);
    setPreviewUrl(URL.createObjectURL(file));

    mutate(file, {
      onSuccess: (data) => {
        const plate =
          data?.plate_number ??
          data?.plateNumber ??
          data?.plate ??
          "";

        if (!plate) {
          setOcrError("번호 인식 실패");
          return;
        }

        const cleaned = plate.replace(/\s+/g, "");
        setPlateNumber(cleaned);

        // ⭐⭐⭐ 전역 세션 저장 (핵심)
        queryClient.setQueryData(["entry-session"], {
          plateNumber: cleaned,
          file: file,
        });
      },
      onError: () => {
        setOcrError("OCR 실패");
      },
    });
  };

  return (
    <div className="entry-exit-page">
      <div className="entry-exit-topbar">
        <div className="entry-exit-title">입차 / 출차</div>
        <button
          className="back-button"
          type="button"
          onClick={() => navigate("/")}
        >
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
              <div className="upload-placeholder">
                사진이 여기에 표시됩니다
              </div>
            )}
          </div>

          {isPending && <div className="ocr-status">인식 중...</div>}
          {isError && <div className="ocr-error">OCR 요청 실패</div>}
          {ocrError && <div className="ocr-error">{ocrError}</div>}
        </div>

        <div className="action-panel">
          <button
            className="action-button"
            type="button"
            onClick={() => navigate("/entry-confirmation")}
          >
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