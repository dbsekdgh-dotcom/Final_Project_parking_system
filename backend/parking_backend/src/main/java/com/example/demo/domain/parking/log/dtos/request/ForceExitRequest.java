package com.example.demo.domain.parking.log.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ForceExitRequest { //강제출차 시 사유(reason)을 받기위함.
    private String reason;
}
