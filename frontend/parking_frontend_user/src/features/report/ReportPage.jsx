import React, { useState } from "react";
import "./ReportPage.css";

// -----------------------------------------------------------------------
// [1단계] 아래 import들을 추가하세요
// -----------------------------------------------------------------------
// import { useMyReports } from "../hooks/useReportQuery";
// import { useReceivedReports } from "../hooks/useReportQuery";
// -----------------------------------------------------------------------
// useReportQuery.js 파일을 hooks 폴더에 새로 만들고 아래처럼 작성하세요:
//
// import { useQuery } from "@tanstack/react-query";
// import { fetchMyReports, fetchReceivedReports } from "../api/ReportApi";
//
// export const useMyReports = () => {
//   return useQuery({
//     queryKey: ["reports", "sent"],
//     queryFn: fetchMyReports,
//   });
// };
//
// export const useReceivedReports = () => {
//   return useQuery({
//     queryKey: ["reports", "received"],
//     queryFn: fetchReceivedReports,
//   });
// };
// -----------------------------------------------------------------------

// -----------------------------------------------------------------------
// [2단계] ReportApi.js 파일을 api 폴더에 새로 만들고 아래처럼 작성하세요:
// -----------------------------------------------------------------------
// import axios from "axios";
// const BASE = "http://localhost:8081/api/v1/reports";
//
// export const fetchMyReports = async () => {
//   const res = await axios.get(`${BASE}/sent`);
//   return res.data; // 백엔드 응답 배열이 그대로 반환됨
// };
//
// export const fetchReceivedReports = async () => {
//   const res = await axios.get(`${BASE}/received`);
//   return res.data;
// };
//
// export const cancelReport = async (reportId) => {
//   const res = await axios.patch(`${BASE}/${reportId}/cancel`);
//   return res.data;
// };
// -----------------------------------------------------------------------

// -----------------------------------------------------------------------
// [3단계] DUMMY_DATA를 지우고 훅으로 교체하세요
// -----------------------------------------------------------------------
// 아래 DUMMY_DATA 전체를 삭제하고,
// 컴포넌트 안에 아래 코드를 넣으세요:
//
// const { data: sentData = [], isLoading: sentLoading } = useMyReports();
// const { data: receivedData = [], isLoading: receivedLoading } = useReceivedReports();
// const listData = activeTab === "sent" ? sentData : receivedData;
// const isLoading = activeTab === "sent" ? sentLoading : receivedLoading;
// -----------------------------------------------------------------------
const DUMMY_DATA = [
  {
    id: 1,
    plateNumber: "12가 3456",
    category: "이중주차",
    date: "2026.03.19 14:00",
    location: "B1층에서 이중 주차 발견",
    status: "승인",
  },
  {
    id: 2,
    plateNumber: "34나 5678",
    category: "통로 막음",
    date: "2026.03.19 14:00",
    location: "B2층 통로 막음",
    status: "대기",
    canCancel: true,
  },
  {
    id: 3,
    plateNumber: "34나 5678",
    category: "통로 막음",
    date: "2026.03.19 14:00",
    location: "B2층 통로 막음",
    status: "취소",
  },
  {
    id: 4,
    plateNumber: "34나 5678",
    category: "기타",
    date: "2026.03.19 14:00",
    location: "쓰레기 무단 투척",
    status: "거절",
  },
  {
    id: 5,
    plateNumber: "12가 3456",
    category: "경적/소음",
    date: "2026.03.19 14:00",
    location: "B1층에서 소음 발생",
    status: "승인",
  },
];

// -----------------------------------------------------------------------
// [4단계] 백엔드 응답 필드명이 다를 경우 맞춰주세요
// 예) 백엔드가 plate_number로 내려주면 item.plateNumber → item.plate_number
// 예) 백엔드가 created_at으로 내려주면 item.date → item.created_at
// 예) 백엔드가 reportStatus로 내려주면 item.status → item.reportStatus
// -----------------------------------------------------------------------

const STATUS_CLASS = {
  승인: "badge badge--approved",
  대기: "badge badge--pending",
  취소: "badge badge--cancelled",
  거절: "badge badge--rejected",
};

export default function ReportPage() {
  const [activeTab, setActiveTab] = useState("sent"); // sent | received

  // -----------------------------------------------------------------------
  // [3단계 적용 위치] 여기에 훅을 추가하세요:
  // const { data: sentData = [], isLoading: sentLoading } = useMyReports();
  // const { data: receivedData = [], isLoading: receivedLoading } = useReceivedReports();
  // const listData = activeTab === "sent" ? sentData : receivedData;
  // const isLoading = activeTab === "sent" ? sentLoading : receivedLoading;
  // -----------------------------------------------------------------------

  // -----------------------------------------------------------------------
  // [5단계] 신고 취소 mutation 추가 (취소하기 버튼 연동)
  // -----------------------------------------------------------------------
  // useCancelReport.js 파일을 hooks 폴더에 새로 만들고 아래처럼 작성하세요:
  //
  // import { useMutation, useQueryClient } from "@tanstack/react-query";
  // import { cancelReport } from "../api/ReportApi";
  //
  // export const useCancelReport = () => {
  //   const queryClient = useQueryClient();
  //   return useMutation({
  //     mutationFn: cancelReport,
  //     onSuccess: () => {
  //       queryClient.invalidateQueries(["reports"]); // 목록 자동 새로고침
  //     },
  //   });
  // };
  //
  // 이 파일 상단 import에 추가:
  // import { useCancelReport } from "../hooks/useCancelReport";
  //
  // 이 위치에 추가:
  // const { mutate: cancelMutate } = useCancelReport();
  // -----------------------------------------------------------------------

  return (
    <div className="report-page">
      {/* 헤더 */}
      <div className="report-header">
        <h2 className="report-title">신고/민원 내역</h2>
        <button className="search-btn">
          <span className="search-icon">📅</span> 검색
          <div className="search-sub">→ 기간별 신고내역 검색</div>
        </button>
      </div>

      {/* 탭 */}
      <div className="report-tabs">
        <button
          className={`tab-btn ${activeTab === "sent" ? "tab-btn--active" : ""}`}
          onClick={() => setActiveTab("sent")}
        >
          내가 신고한 내역
        </button>
        <button
          className={`tab-btn ${activeTab === "received" ? "tab-btn--active" : ""}`}
          onClick={() => setActiveTab("received")}
        >
          내가 받은 신고
        </button>
      </div>

      {/* 목록 */}
      {/* -----------------------------------------------------------------------
          [3단계 적용 위치] DUMMY_DATA → listData 로 교체하세요
          isLoading 처리도 추가하면 좋아요:

          {isLoading ? (
            <div className="loading">불러오는 중...</div>
          ) : (
            listData.map((item) => ( ... ))
          )}
          ----------------------------------------------------------------------- */}
      <div className="report-list">
        {DUMMY_DATA.map((item) => (
          <div className="report-card" key={item.id}>
            <div className="report-card__icon">
              <div className="icon-box">🅿</div>
            </div>

            <div className="report-card__body">
              <div className="report-card__top">
                {/* [4단계] 백엔드 필드명이 다르면 item.plateNumber 수정 */}
                <span className="plate-number">{item.plateNumber}</span>
                {/* [4단계] 백엔드 필드명이 다르면 item.status 수정 */}
                <span className={STATUS_CLASS[item.status]}>{item.status}</span>
              </div>
              {/* [4단계] 백엔드 필드명이 다르면 item.category 수정 */}
              <div className="report-card__category">{item.category}</div>
              {/* [4단계] 백엔드 필드명이 다르면 item.date 수정 */}
              <div className="report-card__date">{item.date}</div>
              {/* [4단계] 백엔드 필드명이 다르면 item.location 수정 */}
              <div className="report-card__location">{item.location}</div>
            </div>

            {item.canCancel && (
              <div className="report-card__action">
                {/* -------------------------------------------------------
                    [5단계] onClick에 취소 mutation 연결하세요:
                    onClick={() => cancelMutate(item.id)}
                    ------------------------------------------------------- */}
                <button className="cancel-btn">신고 취소하기</button>
              </div>
            )}
          </div>
        ))}
      </div>

      {/* 페이지네이션 */}
      {/* -----------------------------------------------------------------------
          [6단계] 페이지네이션 실제 연동 방법:
          1. 상태 추가: const [currentPage, setCurrentPage] = useState(1);
          2. API 함수에 page 파라미터 추가:
             fetchMyReports = async (page) => axios.get(`${BASE}/sent?page=${page}`)
          3. useQuery에 page 전달:
             queryKey: ["reports", "sent", currentPage]
             queryFn: () => fetchMyReports(currentPage)
          4. 버튼 onClick에 setCurrentPage(p) 연결
          5. 백엔드 응답에서 totalPages를 받아서 페이지 수를 동적으로 렌더링
          ----------------------------------------------------------------------- */}
      <div className="pagination">
        <button className="page-btn page-btn--arrow">«</button>
        {[1, 2, 3, 4, 5].map((p) => (
          <button
            key={p}
            className={`page-btn ${p === 1 ? "page-btn--active" : ""}`}
          >
            {p}
          </button>
        ))}
        <button className="page-btn page-btn--arrow">»</button>
      </div>

      {/* 하단 신고하기 버튼 */}
      {/* -----------------------------------------------------------------------
          [7단계] 신고하기 버튼 클릭 시 신고 작성 페이지로 이동하거나 모달을 여세요:
          1. 상단에 import { useNavigate } from "react-router-dom"; 추가
          2. 컴포넌트 안에 const navigate = useNavigate(); 추가
          3. onClick에 연결: onClick={() => navigate("/report/new")}
          또는 모달을 사용한다면 모달 열기 state를 연결하세요
          ----------------------------------------------------------------------- */}
      <div className="report-footer">
        <button className="report-submit-btn">신고하기</button>
      </div>
    </div>
  );
}
