package com.example.demo.domain.payment.ticket.service;

import com.example.demo.domain.store.repository.StoreTicketConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FreeTicketProvideListener implements MessageListener {

    private final StoreTicketConfigRepository storeTicketConfigRepository;

    @Override
    public void onMessage(Message message, @Nullable byte[] pattern) {

    }
}
