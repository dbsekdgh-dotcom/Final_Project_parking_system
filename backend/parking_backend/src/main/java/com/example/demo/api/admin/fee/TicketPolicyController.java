package com.example.demo.api.admin.fee;

import com.example.demo.domain.parking.policy.service.TicketPolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
}
