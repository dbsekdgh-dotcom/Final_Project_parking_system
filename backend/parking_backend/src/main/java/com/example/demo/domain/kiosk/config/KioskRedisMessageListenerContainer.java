package com.example.demo.domain.kiosk.config;

import com.example.demo.domain.kiosk.exit.service.FreeExitExpirationService;
import com.example.demo.domain.kiosk.exit.service.FreeExitKeyExpirationListener;
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
            FreeExitKeyExpirationListener listener) {
        factory.getConnection().serverCommands().setConfig("notify-keyspace-events","Ex");

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener(listener, new PatternTopic("__keyevent@*__:expired"));
        return container;
    }
}
