package com.example.demo.domain.parking.space.dtos.request;

import com.example.demo.domain.parking.space.enums.SpaceUpdateType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SpaceControlRequest { //관리자 요청 받는 DTO
    private SpaceUpdateType action;
}
