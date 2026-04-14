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

//날짜 포맷 함수 (에어 방어막 강화)
function formatDate(isoString) {
  try{
    if (!isoString) return "날짜없음";
    const d = new Date(isoString);

    //날짜 형식이 이상할 경우 방어
    if (isNaN(d.getTime())) return "유효하지 않은 날짜"; 

    const yy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, "0");
    const dd = String(d.getDate()).padStart(2, "0");
    const hh = String(d.getHours()).padStart(2, "0");
    const min = String(d.getMinutes()).padStart(2, "0");
    return `${yy}.${mm}.${dd} ${hh}:${min}`;
  }catch(e){
    //예상치 못할 에러가 발생해도 프로그램이 멈추지 않게 함
    console.error("날짜 변환 에러:",  e);
    return "날짜 오류";
  }
}

export default function ReportPage() {
  const [activeTab, setActiveTab] = useState("sent"); // sent | received
  const [currentPage, setCurrentPage] = useState(0); // 0-based (Spring Pageable)

  const { data: sentPage, isLoading: sentLoading} = useMyReports(currentPage);
  const { data: receivedPage, isLoading: receivedLoading} = useReceivedReports(currentPage);

  // 신고 취소 mutate(로딩 상태 추가 가능)
  const { mutate: cancelMutate } = useCancelReport();

  const isLoading = activeTab === "sent" ? sentLoading : receivedLoading;
  const pageData = activeTab === "sent" ? sentPage : receivedPage;
  const listData = (pageData && Array.isArray(pageData.content))? pageData.content : [];
  const totalPages = pageData?.totalPages ?? 1;

  // 탭 전환 시 첫 페이지로 초기화
  const handleTabChange = (tab) => {
    setActiveTab(tab);
    setCurrentPage(0);
  };

  //취소 핸들러 (Confirm 추가)
  const handleCancel = (reportId) => {
    if(window.confirm("정말 신고를 취소하시겠습니까? 취소 후에는 되돌릴 수 없습니다.")){
      cancelMutate(reportId);
    }
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

            // item이 null이거나 undefined면 에러 방지를 위해 통과
            if (!item) return null;
            console.log("신고 데이터 항목:",item)//디벙깅 후 삭제해야됨

            //필드값이 없을 때를 대비해 기본값(||) 설정
            const rawStatus = item.status || "PENDING";
            const rawType = item.reportType || "OTHER";

            const statusKo = STATUS_KO[rawStatus] ?? rawStatus;
            const typeKo = REPORT_TYPE_KO[rawType] ?? rawType;

            //취소된 항목인지 확인
            const isCancelled = item.status ==="CANCELLED";

            //uniqueKry 설정 (데이터에 따라 reportId 또는 id 사용)
            const uniqueKey = item.reportId || item.id || `report-${Math.random()}`;

            return (
              <div key={uniqueKey}
                className={`report-card ${isCancelled ? "report-card--cancelled" : ""}`}
              >
                <div className="report-card__icon">
                  <div className="icon-box">🅿</div>
                </div>

                <div className="report-card__body">
                  <div className="report-card__top">
                    <span className="plate-number">{item.carNumber}</span>
                    <span className={STATUS_CLASS[statusKo] ?? "badge"}>{statusKo}</span>
                  </div>
                  <div className="report-card__category">
                    {typeKo} {isCancelled && <span className="cancel-label">(취소됨)</span>}
                  </div>
                  <div className="report-card__date">{formatDate(item.createdAt)}</div>
                  <div className="report-card__location">{item.description}</div>
                </div>

                {/* 버튼 제어 : 취소되지 않은 '대기' 상태일 때만 취소버튼 보여주기 */}
                {activeTab === "sent" && item.status ==="PENDING" && (
                  <div className="report-card__action">
                    <button
                      className="cancel-btn"
                      onClick={() => handleCancel(uniqueKey)}
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
