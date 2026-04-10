import React, { useState } from "react";
import "./ReportPage.css";
import { useMyReports, useReceivedReports } from "./hooks/useReportQuery";
import { useCancelReport } from "./hooks/useCancelReport";

// 백엔드 enum → 한글 매핑
const STATUS_KO = {
  PENDING: "대기",
  APPROVED: "승인",
  REJECTED: "거절",
  CANCELLED: "취소",
};

const REPORT_TYPE_KO = {
  DOUBLE_PARK: "이중주차",
  BLOCKING: "통로 막음",
  NOISE: "경적/소음",
  ILLEGAL_PARKING: "불법주차",
  OTHER: "기타",
};

const STATUS_CLASS = {
  대기: "badge badge--pending",
  승인: "badge badge--approved",
  거절: "badge badge--rejected",
  취소: "badge badge--cancelled",
};

function formatDate(isoString) {
  if (!isoString) return "";
  const d = new Date(isoString);
  const yy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const dd = String(d.getDate()).padStart(2, "0");
  const hh = String(d.getHours()).padStart(2, "0");
  const min = String(d.getMinutes()).padStart(2, "0");
  return `${yy}.${mm}.${dd} ${hh}:${min}`;
}

export default function ReportPage() {
  const [activeTab, setActiveTab] = useState("sent"); // sent | received
  const [currentPage, setCurrentPage] = useState(0); // 0-based (Spring Pageable)

  const {
    data: sentPage,
    isLoading: sentLoading,
  } = useMyReports(currentPage);

  const {
    data: receivedPage,
    isLoading: receivedLoading,
  } = useReceivedReports(currentPage);

  const { mutate: cancelMutate } = useCancelReport();

  const isLoading = activeTab === "sent" ? sentLoading : receivedLoading;
  const pageData = activeTab === "sent" ? sentPage : receivedPage;
  const listData = pageData?.content ?? [];
  const totalPages = pageData?.totalPages ?? 1;

  // 탭 전환 시 첫 페이지로 초기화
  const handleTabChange = (tab) => {
    setActiveTab(tab);
    setCurrentPage(0);
  };

  return (
    <div className="report-page">
      {/* 헤더 */}
      <div className="report-header">
        <h2 className="report-title">신고/민원 내역</h2>
      </div>

      {/* 탭 */}
      <div className="report-tabs">
        <button
          className={`tab-btn ${activeTab === "sent" ? "tab-btn--active" : ""}`}
          onClick={() => handleTabChange("sent")}
        >
          내가 신고한 내역
        </button>
        <button
          className={`tab-btn ${activeTab === "received" ? "tab-btn--active" : ""}`}
          onClick={() => handleTabChange("received")}
        >
          내가 받은 신고
        </button>
      </div>

      {/* 목록 */}
      <div className="report-list">
        {isLoading ? (
          <div className="loading">불러오는 중...</div>
        ) : listData.length === 0 ? (
          <div className="loading">내역이 없습니다.</div>
        ) : (
          listData.map((item) => {
            const statusKo = STATUS_KO[item.status] ?? item.status;
            const typeKo = REPORT_TYPE_KO[item.reportType] ?? item.reportType;
            const canCancel = activeTab === "sent" && item.status === "PENDING";

            return (
              <div className="report-card" key={item.id}>
                <div className="report-card__icon">
                  <div className="icon-box">🅿</div>
                </div>

                <div className="report-card__body">
                  <div className="report-card__top">
                    <span className="plate-number">{item.carNumber}</span>
                    <span className={STATUS_CLASS[statusKo] ?? "badge"}>
                      {statusKo}
                    </span>
                  </div>
                  <div className="report-card__category">{typeKo}</div>
                  <div className="report-card__date">{formatDate(item.createdAt)}</div>
                  <div className="report-card__location">{item.description}</div>
                </div>

                {canCancel && (
                  <div className="report-card__action">
                    <button
                      className="cancel-btn"
                      onClick={() => cancelMutate(item.id)}
                    >
                      신고 취소하기
                    </button>
                  </div>
                )}
              </div>
            );
          })
        )}
      </div>

      {/* 페이지네이션 */}
      <div className="pagination">
        <button
          className="page-btn page-btn--arrow"
          onClick={() => setCurrentPage((p) => Math.max(0, p - 1))}
          disabled={currentPage === 0}
        >
          «
        </button>
        {Array.from({ length: totalPages }, (_, i) => (
          <button
            key={i}
            className={`page-btn ${i === currentPage ? "page-btn--active" : ""}`}
            onClick={() => setCurrentPage(i)}
          >
            {i + 1}
          </button>
        ))}
        <button
          className="page-btn page-btn--arrow"
          onClick={() => setCurrentPage((p) => Math.min(totalPages - 1, p + 1))}
          disabled={currentPage >= totalPages - 1}
        >
          »
        </button>
      </div>

      {/* 하단 신고하기 버튼 */}
      <div className="report-footer">
        <button className="report-submit-btn">신고하기</button>
      </div>
    </div>
  );
}
