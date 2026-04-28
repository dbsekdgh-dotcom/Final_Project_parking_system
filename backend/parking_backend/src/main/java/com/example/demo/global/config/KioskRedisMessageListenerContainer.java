package com.example.demo.global.config;

import com.example.demo.domain.parking.exit.service.FreeExitKeyExpirationListener;
import com.example.demo.domain.payment.ticket.service.FreeTicketProvideListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class KioskRedisMessageListenerContainer {
    @Bean
    public RedisMessageListenerContainer freeExitRedisListenerContainer(
            RedisConnectionFactory factory,
            FreeExitKeyExpirationListener freeExitListener,
            FreeTicketProvideListener freeTicketListener) {
        factory.getConnection().serverCommands().setConfig("notify-keyspace-events", "Ex");

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener(freeExitListener, new PatternTopic("__keyevent@*__:expired"));
        container.addMessageListener(freeTicketListener, new PatternTopic("__keyevent@*__:expired"));
        return container;
    }
}
