package com.example.demo.api.admin.parkingspace;


import com.example.demo.domain.parking.space.dtos.request.SpaceControlRequest;
import com.example.demo.domain.parking.space.dtos.response.ParkingSpaceListResponse;
import com.example.demo.domain.parking.space.dtos.response.ParkingSpaceSummaryResponse;
import com.example.demo.domain.parking.space.service.AdminParkingSpaceService;
import com.example.demo.domain.parking.space.enums.Floor;
import com.example.demo.global.common.ApiResponse;
import com.example.demo.global.security.admin.AdminAuthDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/parking-space")
public class AdminParkingSpaceController {
    private final AdminParkingSpaceService adminParkingSpaceService;

    //관리자 - 주차공간 상단 요약정보
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<ParkingSpaceSummaryResponse>> getSummary(){
        return ResponseEntity.ok(ApiResponse.success(adminParkingSpaceService.getParkingSpaceSummary()));
    }

    //관리자 - 주차공간 하단 층별 리스트
    @GetMapping
    public ResponseEntity<ApiResponse<List<ParkingSpaceListResponse>>> getFloorSpaces(@RequestParam(name = "floor") Floor floor) {
        log.info("관리자가 {}층 주차 현황을 조회합니다.",floor);
        return ResponseEntity.ok(ApiResponse.success(adminParkingSpaceService.getFloorSpaces(floor)));
    }

    //관리자 - 주차 공간 구획별 통합 제어
    @PatchMapping("/{spaceId}/control")
    public ResponseEntity<ApiResponse<String>> controlParkingSpace(
            @PathVariable(name = "spaceId") Long spaceId,
            @RequestBody SpaceControlRequest request,
            @AuthenticationPrincipal AdminAuthDto adminAuthDto
            ) throws Exception{
        log.info("관리자 {}가 주차 공간 {}의 제어를 요청했습니다. 액션: {}",adminAuthDto.getUsername(),spaceId,request.getAction());

        adminParkingSpaceService.controlParkingSpace(spaceId,request,adminAuthDto);
        return ResponseEntity.ok(ApiResponse.success("주차 공간 상태가 성공적으로 변경되었습니다."));
    }
}
