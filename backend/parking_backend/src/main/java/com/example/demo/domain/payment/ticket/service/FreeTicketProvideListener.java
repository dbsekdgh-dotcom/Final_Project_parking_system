package com.example.demo.domain.payment.ticket.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FreeTicketProvideListener implements MessageListener {

    private static final String KEY_PREFIX = "store:free-ticket:";

    private final FreeTicketProvideService freeTicketProvideService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        if (!expiredKey.startsWith(KEY_PREFIX)) return;

        try {
            Long storeId = Long.parseLong(expiredKey.substring(KEY_PREFIX.length()));
            freeTicketProvideService.provide(storeId);
        } catch (Exception e) {
            log.error("store:free-ticket 만료 처리 실패={}: {}", expiredKey, e.getMessage());
        }
    }
}
