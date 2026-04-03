import React, { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./entryExit.css";

import { usePlateOCRMutation } from "../../entry/hooks/usePlateImage";
import { useEntryMutation } from "../../entry/hooks/UseEntryMutate";
import { useQueryClient } from "@tanstack/react-query";

const SLOT_COUNT = 8;

export default function EntryExit() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  // ✅ OCR mutation
  const {
    mutate: ocrMutate,
    isPending: ocrLoading,
    isError: ocrError,
  } = usePlateOCRMutation();

  // ✅ 입차 mutation (S3 + DB)
  const {
    mutate: entryMutate,
    isPending: entryLoading,
  } = useEntryMutation();

  const [previewUrl, setPreviewUrl] = useState(null);
  const [plateNumber, setPlateNumber] = useState("");

  useEffect(() => {
    return () => {
      if (previewUrl) URL.revokeObjectURL(previewUrl);
    };
  }, [previewUrl]);

  const plateChars = useMemo(() => {
    const chars = Array.from(plateNumber);
    return Array.from({ length: SLOT_COUNT }, (_, i) => chars[i] ?? "");
  }, [plateNumber]);


  const handleFileChange = (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (previewUrl) URL.revokeObjectURL(previewUrl);
    setPreviewUrl(URL.createObjectURL(file));

    ocrMutate(file, {
      onSuccess: (data) => {
        const plate =
          data?.plate_number ??
          data?.plateNumber ??
          data?.plate ??
          "";

        if (!plate) {
          alert("번호판 인식 실패");
          return;
        }

        const cleaned = plate.replace(/\s+/g, "");
        setPlateNumber(cleaned);

       
        queryClient.setQueryData(["entry-session"], {
          plateNumber: cleaned,
          file: file,
        });
      },
      onError: (err) => {
        console.error("OCR 실패:", err);
        alert("OCR 요청 실패");
      },
    });
  };


  const handleEntry = () => {
    const session = queryClient.getQueryData(["entry-session"]);

    if (!session) {
      alert("먼저 차량 사진을 업로드하세요.");
      return;
    }

    entryMutate(session, {
      onSuccess: () => {
        // 성공하면 다음 화면 이동
        navigate("/entry-confirmation");
      },
      onError: (err) => {
        console.error("입차 실패:", err);
        alert("입차 처리 실패");
      },
    });
  };

  return (
    <div className="entry-exit-page">
      {/* ---------- TOP BAR ---------- */}
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

      {/* ---------- MAIN ---------- */}
      <div className="entry-exit-layout">
        {/* 번호판 영역 */}
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

          {ocrLoading && (
            <div className="ocr-status">번호판 인식 중...</div>
          )}
          {ocrError && (
            <div className="ocr-error">OCR 요청 실패</div>
          )}
        </div>

        {/* 버튼 영역 */}
        <div className="action-panel">
          <button
            className="action-button"
            type="button"
            onClick={handleEntry}
            disabled={entryLoading}
          >
            {entryLoading ? "입차 처리 중..." : "입차"}
          </button>

          <button className="action-button" type="button">
            출차
          </button>
        </div>
      </div>
    </div>
  );
}