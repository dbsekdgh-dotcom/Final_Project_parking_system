import React, { useState } from 'react'
import '../../../app.css'
import './prepayment.css'
import useVehicleStore from '../../../store/useVehicleStore'
import { useQuery } from '@tanstack/react-query'
import { searchCar } from '../../../shared/api/VehicleApi'
import { useNavigate } from 'react-router-dom'
import VehicleList from '../../../shared/components/vehicleList/VehicleList'
import ResultView from '../../../shared/components/resultView/ResultView'

const VehicleSearchResult = () => {
  const { searchKeyword, setSelectedVehicle } = useVehicleStore();
  const navigate = useNavigate();

  const { data, isLoading, isError, error } = useQuery({
    queryKey: ['vehicle', searchKeyword],
    queryFn: async () => await searchCar(searchKeyword),
    enabled: !!searchKeyword,
    retry: (failureCount, err) => {
      const status = err?.response?.status;
      if (status && status < 500) return false; // 4xx는 재시도 안 함
      return failureCount < 3;
    }
  });

  const selectHandler = (vehicle) => {
    setSelectedVehicle(vehicle)
    navigate("/selectedVehicle")
  }

  return (
    <div className="kiosk-page-root">
      <div className="kiosk-page-wrapper">
        <div className="kiosk-page-header">
          <h2 className="page-title">검색 결과</h2>
          <button type="button" className="header-back-button" onClick={() => navigate(-1)}>돌아가기</button>
        </div>

        <div className="content-area">
          {isLoading &&
            <ResultView
              title="조회 중"
              subTitle="차량 정보를 가져오고 있습니다."
              type="loading"
            />
          }

          {isError &&
            <ResultView
              title="조회 실패"
              subTitle={error?.response?.data?.message || "차량 정보를 가져오는 데 실패했습니다."}
              type="error"
            />
          }

          {data && data.length === 0 &&
            <ResultView
              title="검색 결과 없음"
              subTitle={`${searchKeyword}에 대한 입차 기록이 없습니다.`}
              type="error"
            />
          }

          {!isLoading && !isError && data &&
            <VehicleList vehicles={data} onSelect={selectHandler} />
          }
        </div>

        <button type="button" className="back-button" onClick={() => navigate(-1)} disabled={isLoading}>
          돌아가기
        </button>
      </div>
    </div>
  )
}

export default VehicleSearchResult
