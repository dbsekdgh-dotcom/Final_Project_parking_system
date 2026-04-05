package com.example.demo.domain.user.mypage.point.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PointResponseDto {
    private int currentPoint;
    private List<PointLogDto> history;

}
