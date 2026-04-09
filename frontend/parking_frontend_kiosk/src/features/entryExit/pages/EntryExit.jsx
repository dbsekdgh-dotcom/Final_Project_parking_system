import React, { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./entryExit.css";
import { requestExit } from "../../exit/api/ExitApi";
import { usePlateOCRMutation } from "../../entry/hooks/usePlateImage";
import { useEntryMutation } from "../../entry/hooks/UseEntryMutate";
import { checkVehicleEntered, fetchEntryCameras, fetchExitCameras } from "../../entry/api/EntryApi";
import { useQueryClient } from "@tanstack/react-query";

const SLOT_COUNT = 8;

export default function EntryExit() {
  const [uploadFile, setUploadFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [plateNumber, setPlateNumber] = useState("");
  const [entryCameras, setEntryCameras] = useState([]);
  const [exitCameras, setExitCameras] = useState([]);
  // null: OCR 전, true: 출차 모드, false: 입차 모드
  const [isEntered, setIsEntered] = useState(null);
  const [cameraLoading, setCameraLoading] = useState(false);
  const [imagePath, setImagePath]=useState("");
  const [parkingLogId,setParkingLogId] = useState(null);
  const [exitLoading,setExitLoading] = useState(false);

  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const {
    mutate: ocrMutate,
    isPending: ocrLoading,
    isError: ocrError,
  } = usePlateOCRMutation();

  const {
    mutate: entryMutate,
    isPending: entryLoading,
  } = useEntryMutation();

  // 입차/출차 카메라 목록은 페이지 진입 시 한 번만 조회
  useEffect(() => {
    fetchEntryCameras()
      .then(setEntryCameras)
      .catch((err) => console.error("입차 카메라 목록 조회 실패:", err));
    fetchExitCameras()
      .then(setExitCameras)
      .catch((err) => console.error("출차 카메라 목록 조회 실패:", err));
  }, []);

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

    setUploadFile(file);
    setIsEntered(null);

    if (previewUrl) URL.revokeObjectURL(previewUrl);
    setPreviewUrl(URL.createObjectURL(file));

    ocrMutate(file, {
      onSuccess: async (data) => {
        const plate =
          data?.plate_number ??
          data?.plateNumber ??
          data?.plate ??
          "";
        const s3path = data?.s3path ?? "";

        if (!plate) {
          alert("번호판 인식 실패");
          return;
        }

        const cleaned = plate.replace(/\s+/g, "");
        setPlateNumber(cleaned);
        setImagePath(s3path)
        queryClient.setQueryData(["entry-session"], { plateNumber: cleaned });

        // OCR 성공 후 현재 ENTERED 상태 여부 조회
        setCameraLoading(true);
        try {
          const result = await checkVehicleEntered(cleaned);
          setIsEntered(result.isEntered);
          if (result.parkingLogId){
            setParkingLogId(result.parkingLogId)
          }
        } catch (err) {
          console.error("차량 상태 조회 실패:", err);
          alert("차량 상태 조회 실패");
        } finally {
          setCameraLoading(false);
        }
      },
      onError: (err) => {
        console.error("OCR 실패:", err);
        alert("OCR 요청 실패");
      },
    });
  };

  // 입차 카메라 버튼 클릭
  const handleEntryCamera = (entryCameraId) => {
    if (!plateNumber) {
      alert("먼저 차량 사진을 업로드하세요.");
      return;
    }

    entryMutate(
      { plateNumber, s3path:imagePath, cameraId: entryCameraId },
      {
        onSuccess: (data) => {
          navigate("/entry-parkingspace", { state: { parkingLogId: data.parkingLogId } });
        },
        onError: (err) => {
          if (err?.response?.status === 403) {
            alert("블랙리스트 차량입니다. 입차가 거부되었습니다.");
          } else {
            console.error("입차 실패:", err);
            alert("입차 처리 실패");
          }
          // 화면 유지 (navigate 하지 않음)
        },
      }
    );
  };

  // 출차 카메라 버튼 클릭 (추후 연결)
  const handleExitCamera = async(exitCameraId) => {
    if(!uploadFile){
      alert("먼저 차량 사진을 업로드 하세요.");
      return;
    }
    if(!parkingLogId){
      alert("차량 정보를 확인할 수 없습니다.")
      return;
    }
    
    setExitLoading(true);
    try{
      const data = await requestExit(parkingLogId, exitCameraId,imagePath);
      if (data.isFree){
        navigate("/exit-departure",{
          state:{ parkingLogId, message: data.message}
        });
      }else{
        navigate("/exit-paymentconfirm",{
          state:{
            parkingLogId,
            vehicleNumber: data.vehicleNumber,
            parkingTime: data.parkingTime,
            amountToPay: data.amountToPay,
          }
        });
      }
    }catch(err){
      console.error("출차 요청 실패:",err);
      alert(err?.response?.data?.message||"출차 요청 실패")
    } finally {
      setExitLoading(false);
    }
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

          {ocrLoading && <div className="ocr-status">번호판 인식 중...</div>}
          {ocrError && <div className="ocr-error">OCR 요청 실패</div>}
          {cameraLoading && <div className="ocr-status">차량 상태 확인 중...</div>}
        </div>

        {/* 버튼 영역: OCR + 상태 조회 완료 후 표시 */}
        <div className="action-panel">
          {isEntered === null && (
            <div className="action-placeholder">
              차량 사진을 업로드하면 버튼이 표시됩니다
            </div>
          )}

          {isEntered === false && (
            <>
              <div className="camera-section-label">입구 선택</div>
              {entryCameras.map((cam) => (
                <button
                  key={cam.cameraId}
                  className="action-button"
                  type="button"
                  disabled={entryLoading}
                  onClick={() => handleEntryCamera(cam.cameraId)}
                >
                  {entryLoading
                    ? "처리 중..."
                    : cam.description || cam.location || `${cam.cameraId}번 입구`}
                </button>
              ))}
            </>
          )}

          {isEntered === true && (
            <>
              <div className="camera-section-label">출구 선택</div>
              {exitCameras.map((cam) => (
                <button
                  key={cam.cameraId}
                  className="action-button"
                  type="button"
                  disabled={exitLoading}
                  onClick={() => handleExitCamera(cam.cameraId)}
                >
                  {exitLoading
                  ?"처리 중 ..."
                   :cam.description || cam.location || `${cam.cameraId}번 출구`}
                </button>
              ))}
            </>
          )}
        </div>
      </div>
    </div>
  );
}
