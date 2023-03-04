package com.comeon.websocket.web.config;

import com.comeon.websocket.utils.StompSessionAttrUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingUnsubscribeEventListener {

    private final UserLockRemoveSupporter userLockRemoveSupporter;

    @EventListener
    public void handleDisconnectedEvent(SessionDisconnectEvent event) {
        log.debug("MeetingUnsubscribeEventListener.handleDisconnectedEvent()");
        SimpMessageHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(event.getMessage(), SimpMessageHeaderAccessor.class);
        if (accessor != null && accessor.getSessionAttributes() != null) {
            userLockRemoveSupporter.userLockRemove(StompSessionAttrUtils.getUserId(accessor.getSessionAttributes()));
        }
    }
}
