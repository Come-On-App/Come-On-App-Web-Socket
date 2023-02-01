package com.comeon.websocket.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
public class WebSocketEventListener {

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        log.info("start disconnect event");
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = headerAccessor.getUser().getName();
        log.info("principal: {}", username);
        log.info("sessionId: {}", headerAccessor.getSessionId());
        log.info("sessionAttr: {}", headerAccessor.getSessionAttributes().toString());
        log.info("end disconnect event");

    }
}
