package com.example.demo.domain.parking.space.enums;

public enum SpaceUpdateType { // 관리자 요청 타입을 정의
    BLOCK, //차단하기
    UNBLOCK, //해제하기
    SET_DISABLED, //장애인석으로 설정
    SET_EV, //전기차석으로 설정
    SET_GENERAL //일반석으로 설정
}
