package com.comeon.websocket.test;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;


@Slf4j
@Controller
@RequiredArgsConstructor
public class MessageTestController {

    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/message")
    public void sessionUserTest(Principal principal,  @Payload String message, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        log.info("sessionId: {}", sessionId);
        log.info("principal: {}", principal.getName());
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        log.info("session attributes: {}", sessionAttributes.toString());
        Long uid = Long.valueOf(sessionAttributes.get("uid").toString());
        log.info("uid: {}", uid);
        simpMessagingTemplate.setHeaderInitializer(ha -> ha.setContentType(APPLICATION_JSON));
        simpMessagingTemplate.convertAndSend("/sub", Map.of("userId", uid, "message", message));
    }
}
