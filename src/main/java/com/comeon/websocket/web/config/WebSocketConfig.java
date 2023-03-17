package com.comeon.websocket.web.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.WebSocketMessageBrokerStats;
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
    private final SubscribeInterceptor subscribeInterceptor;
    private final UnsubscribeInterceptor unsubscribeInterceptor;
    private final DisconnectInterceptor disconnectInterceptor;

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
        // 클라이언트가 메시지 발행시 /topic/* 경로로 전송
        registry.setApplicationDestinationPrefixes("/topic");
        // 클라이언트가 메시지를 /sub/* 경로로 구독
        registry.enableSimpleBroker("/sub", "/queue");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(subscribeInterceptor, unsubscribeInterceptor, disconnectInterceptor);
    }

    @Profile("local")
    @Bean
    public WebSocketMessageBrokerStats localWebSocketMessageBrokerStats(WebSocketMessageBrokerStats webSocketMessageBrokerStats) {
        webSocketMessageBrokerStats.setLoggingPeriod(30 * 1000);
        return webSocketMessageBrokerStats;
    }

//    @Bean
//    public SimpleBrokerMessageHandler customSimpleBrokerMessageHandler(SimpleBrokerMessageHandler simpleBrokerMessageHandler) {
//        simpleBrokerMessageHandler.setSubscriptionRegistry(customSubscriptionRegistry);
//        return simpleBrokerMessageHandler;
//    }
}
