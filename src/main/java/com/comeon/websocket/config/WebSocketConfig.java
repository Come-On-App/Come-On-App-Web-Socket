package com.comeon.websocket.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;
    private final JwtHandshakeHandler jwtHandshakeHandler;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // without SockJS
        registry.addEndpoint("/ws-meetings")
                .setAllowedOrigins("http://localhost:8288")
                .setHandshakeHandler(jwtHandshakeHandler)
                .addInterceptors(jwtHandshakeInterceptor);

        // with SockJS
        registry.addEndpoint("/ws-meetings")
                .setAllowedOrigins("http://localhost:8288")
                .addInterceptors(jwtHandshakeInterceptor)
                .setHandshakeHandler(jwtHandshakeHandler)
                .withSockJS()
                .setDisconnectDelay(15 * 1000)
                .setHeartbeatTime(5 * 1000);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트가 메시지 발행시 /pub/* 경로로 전송
        registry.setApplicationDestinationPrefixes("/pub");
        // 클라이언트가 메시지를 /sub/* 경로로 구독
        registry.enableSimpleBroker("/sub");
    }
}
