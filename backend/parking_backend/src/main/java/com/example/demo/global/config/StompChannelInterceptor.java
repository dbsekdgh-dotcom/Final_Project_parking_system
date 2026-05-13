package com.example.demo.global.config;

import com.example.demo.domain.auth.admin.repository.AdminRepository;
import com.example.demo.global.security.admin.AdminAuthDto;
import com.example.demo.global.util.admin.AdminJWTUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StompChannelInterceptor implements ChannelInterceptor {

    private final AdminJWTUtil adminJWTUtil;
    private final AdminRepository adminRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message,StompHeaderAccessor.class);

        // CONNECT 프레임에서만 JWT 검증
        if (StompCommand.CONNECT.equals(accessor.getCommand())){
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")){
                String token = authHeader.substring(7);
                Claims claims = adminJWTUtil.validateToken(token);
                String loginId = (String) claims.get("loginId");
                String name = (String) claims.get("name");

                AdminAuthDto adminAuthDto = new AdminAuthDto(loginId,"pw_hidden", name);
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(adminAuthDto,null,adminAuthDto.getAuthorities());
                accessor.setUser(auth);
            }
        }
        return message;
    }
}
