package com.example.demo.domain.payment.point.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PointLogDto {

    private int changeAmount;
    private int beforePoint;
    private int afterPoint;
    private String reason;
    private String description;
    private LocalDateTime createdAt;
}
