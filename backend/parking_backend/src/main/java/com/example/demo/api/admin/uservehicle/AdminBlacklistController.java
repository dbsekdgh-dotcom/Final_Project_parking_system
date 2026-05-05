package com.example.demo.api.admin.uservehicle;

import com.example.demo.domain.management.dtos.request.AdminBlacklistRequestDto;
import com.example.demo.domain.management.dtos.response.AdminBlacklistDetailResponseDto;
import com.example.demo.domain.management.dtos.response.AdminBlacklistResponseDto;
import com.example.demo.domain.management.service.AdminBlacklistService;
import com.example.demo.domain.vehicle.blacklist.enums.BlacklistReasonType;
import com.example.demo.domain.vehicle.blacklist.enums.BlacklistStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/management/blacklist")
@RequiredArgsConstructor
public class AdminBlacklistController {

    private final AdminBlacklistService adminBlacklistService;

    //블랙리스트등록 API
    @PostMapping
    public ResponseEntity<Long> register(@RequestBody AdminBlacklistRequestDto requestDto){
        return ResponseEntity.ok(adminBlacklistService.register(requestDto));
    }

    //블랙리스트 목록 조회API
    @GetMapping
    public ResponseEntity<Page<AdminBlacklistResponseDto>> getBlacklist(
            @RequestParam(required = false) String carNumber,
            @RequestParam(required = false) BlacklistStatus status,
            @RequestParam(required = false) BlacklistReasonType reasonType,
            @PageableDefault(size = 10)Pageable pageable){

        return ResponseEntity.ok(adminBlacklistService.getBlackList(carNumber, status,reasonType,pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminBlacklistDetailResponseDto> getDetail(@PathVariable Long id){
        return ResponseEntity.ok(adminBlacklistService.getDetail(id));
    }
}
