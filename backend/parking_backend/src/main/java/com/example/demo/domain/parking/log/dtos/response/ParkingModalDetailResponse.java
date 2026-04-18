package com.example.demo.domain.parking.log.dtos.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ParkingModalDetailResponse {
    private long rawFee; //원금
    private int storeDiscountTotal; //상가 할인 합계(STORE 타입 합산)
    private int adminDiscountTotal; //관리자 할인 합계(ADMIN 타입 합산)
    private long finalFee; //최종 결제 금액
}
