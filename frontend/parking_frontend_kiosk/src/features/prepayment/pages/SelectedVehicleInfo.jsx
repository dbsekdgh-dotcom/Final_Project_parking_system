import React, { useEffect, useState } from 'react'
import VehicleInfo from '../../../shared/components/vehicleInfo/VehicleInfo'
import ResultView from '../../../shared/components/resultView/ResultView'
import useVehicleStore from '../../../store/useVehicleStore';
import '../../../app.css'
import PaymentMethod from '../../../shared/components/paymentMethod/PaymentMethod';
import { useQuery } from '@tanstack/react-query';
import { requestPayment, cancelPayment } from '../../../shared/api/VehicleApi';
import { useNavigate } from 'react-router-dom';
import { usePayment } from '../../../shared/hooks/usePaymentMutation';

const SelectedVehicleInfo = () => {
  const { selectedVehicle, resetSearchKeyword, resetSelectedVehicle } = useVehicleStore();
  const { beforeMutation } = usePayment();
  const [isPaymentLoading, setIsPaymentLoading] = useState(false)
  const [queryError, setQueryError] = useState(null)
  const navigate = useNavigate();

  const { data, isError, error, isLoading } = useQuery({
    queryKey: ['selectedVehicle', selectedVehicle],
    queryFn: async () => await requestPayment(selectedVehicle),
    enabled: !!selectedVehicle,
    staleTime: Infinity,
    refetchOnWindowFocus: false,
    retry: (failureCount, err) => err?.response?.status === 409 ? false : failureCount < 3
  })
  const prepaid=(data?.calculatedFee ?? 0)-(data?.amountToPay ?? 0);

  useEffect(() => {
    if (isError) {
      setQueryError(error?.response?.data?.message || error?.message || "서버와의 통신이 원활하지 않습니다.")
      resetSelectedVehicle()
    }
  }, [isError])

  const homeHandler = () => {
    if (data?.vehicleNumber) {
      cancelPayment(data.vehicleNumber)
    }
    resetSearchKeyword()
    resetSelectedVehicle()
    navigate('/')
  }

  const paymentHandler = async (paymentData) => {
    if (isPaymentLoading) return;

    if (data?.free) {
      navigate("/PrepaymentResult", {
        state: {
          title: "무료 출차 가능합니다.",
          subTitle: data.message || "이미 등록되었거나 정산할 금액이 없는 차량입니다.",
          type: "success"
        }
      })
      return
    }

    setIsPaymentLoading(true)
    const settlementPayload = {
      "parkingLogId": data.parkingLogId,
      "vehicleNumber": data.vehicleNumber,
      "usedPoint": paymentData.usedPoint || 0,
      "paidAmount": paymentData.paidAmount || 0,
      "settlementType": "PREPAYMENT",
    }
    await beforeMutation.mutateAsync(settlementPayload)
    setIsPaymentLoading(false)
  }

  if (queryError) {
    return <ResultView title="조회 실패" subTitle={queryError} type="error" />
  }

  if (!selectedVehicle) {
    return <ResultView title="조회 정보 없음" subTitle="선택된 차량 정보가 없습니다." type="error" />
  }

  if (isLoading) {
    return <ResultView title="정보 조회 중" subTitle="정산 데이터를 불러오고 있습니다." type="loading" />
  }

  return (
    <div style={{ width: 'var(--wrapper-width)', background: 'var(--panel-light)', border: 'var(--border-std)', padding: 'var(--wrapper-pad)', boxShadow: '20px 20px 0px rgba(0,0,0,0.05)', margin: '60px auto' }}>
        <div className="kiosk-page-header">
          <h2 className="page-title">결제 확인</h2>
          <button className="header-back-button" onClick={homeHandler}>처음으로</button>
        </div>
        <div className="selected-vehicle-info">
          <div>
            <VehicleInfo vehicleNumber={data?.vehicleNumber} parkingTime={data?.parkingTime} totalDiscountMinutes={data?.totalDiscountMinutes} />
          </div>
          <div>
            <div className="fee-summary-box">
              {(data?.totalDiscountMinutes > 0 || data?.totalDiscountAmount > 0) && (
                <div className="fee-summary-row">
                  <span className="fee-summary-label">{data?.totalDiscountMinutes > 0 ? '시간할인 후 요금' : '계산된 요금'}</span>
                  <span className="fee-summary-value">{data?.rawFee?.toLocaleString()}원</span>
                </div>
              )}
              {data?.totalDiscountAmount > 0 && (
                <div className="fee-summary-row">
                  <span className="fee-summary-label">금액 할인</span>
                  <span className="fee-summary-value fee-summary-discount">- {data?.totalDiscountAmount?.toLocaleString()}원</span>
                </div>
              )}
              {prepaid > 0 && (
                <div className="fee-summary-row">
                  <span className="fee-summary-label">이미 결제된 금액</span>
                  <span className="fee-summary-value fee-summary-discount">- {prepaid.toLocaleString()}원</span>
                </div>
              )}
              {(data?.totalDiscountMinutes > 0 || data?.totalDiscountAmount > 0 || prepaid > 0) && (
                <div className="fee-summary-divider" />
              )}
              <div className="fee-summary-row">
                <span className="fee-summary-label">결제 요금</span>
                <span className="fee-summary-total">{data?.amountToPay === 0 ? '무료' : (data?.amountToPay?.toLocaleString() ?? '0') + '원'}</span>
              </div>
            </div>
            <PaymentMethod userPoint={selectedVehicle?.userPoint} fee={data?.amountToPay} onConfirm={paymentHandler} isLoading={isPaymentLoading} />
          </div>
        </div>
      </div>
  )
}

export default SelectedVehicleInfo;

