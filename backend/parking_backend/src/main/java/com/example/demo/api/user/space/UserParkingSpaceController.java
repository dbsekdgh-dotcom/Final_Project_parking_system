package com.example.demo.api.user.space;

import com.example.demo.domain.parking.space.dtos.response.UserParkingSummaryDto;
import com.example.demo.domain.parking.space.service.UserParkingSpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/space")
public class UserParkingSpaceController {

    private final UserParkingSpaceService userParkingSpaceService;

    @GetMapping("/summary")
    public ResponseEntity<UserParkingSummaryDto> getSummary(
            @RequestParam(name = "userType") String userType) {
        return ResponseEntity.ok(userParkingSpaceService.getMyFloorSummary(userType));
    }
}
