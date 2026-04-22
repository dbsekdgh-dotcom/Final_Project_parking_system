package com.example.demo.api.admin.fee;

import com.example.demo.domain.payment.ticketpolicy.dtos.request.TicketPolicyInsertRequestDto;
import com.example.demo.domain.payment.ticketpolicy.service.TicketPolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class TicketPolicyController {
    private final TicketPolicyService ticketPolicyService;

    @DeleteMapping("/ticket-policy")
    public void deleteTicketPolicy(@RequestParam Long ticketPolicyId){
        log.info("삭제 요청 정책 id==>{}",ticketPolicyId);
        ticketPolicyService.deleteTicketPolicy(ticketPolicyId);
    }

    @PostMapping("/ticket-policy")
    public ResponseEntity<Long> insertTicketPolicy(@RequestBody TicketPolicyInsertRequestDto dto){
        long ticketPolicyId=ticketPolicyService.insertTicketPolicy(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketPolicyId);
    }

    @PutMapping("/ticket-policy")
    public void inactivateTicketPolicy(@RequestBody Map<String,Long> map){
        log.info("비활성화 요청 정책 id==>{}",map.get("ticketPolicyId"));
        ticketPolicyService.inactivatePolicy(map.get("ticketPolicyId"));
    }

}
