package com.example.demo.api.admin.fee;

import com.example.demo.domain.payment.ticketpolicy.dtos.request.TicketPolicyInsertRequestDto;
import com.example.demo.domain.payment.ticketpolicy.service.TicketPolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "6. 할인권 정책 (Ticket Policy)", description = "할인권 정책 등록, 삭제, 비활성화 API")
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class TicketPolicyController {
    private final TicketPolicyService ticketPolicyService;

    @Operation(summary = "할인권 정책 삭제", description = "ticketPolicyId로 지정한 할인권 정책을 삭제합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @DeleteMapping("/ticket-policy")
    public void deleteTicketPolicy(@RequestParam Long ticketPolicyId){
        log.info("삭제 요청 정책 id==>{}",ticketPolicyId);
        ticketPolicyService.deleteTicketPolicy(ticketPolicyId);
    }

    @Operation(summary = "할인권 정책 등록", description = "새로운 할인권 정책(이름, 할인율/금액, 유효 기간 등)을 등록합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PostMapping("/ticket-policy")
    public ResponseEntity<Long> insertTicketPolicy(@RequestBody TicketPolicyInsertRequestDto dto){
        long ticketPolicyId=ticketPolicyService.insertTicketPolicy(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketPolicyId);
    }

    @Operation(summary = "할인권 정책 비활성화", description = "지정한 할인권 정책을 비활성화합니다. 비활성화 후에도 기존 잔량은 계속 사용 가능합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PutMapping("/ticket-policy")
    public void inactivateTicketPolicy(@RequestBody Map<String,Long> map){
        log.info("비활성화 요청 정책 id==>{}",map.get("ticketPolicyId"));
        ticketPolicyService.inactivatePolicy(map.get("ticketPolicyId"));
    }
}
