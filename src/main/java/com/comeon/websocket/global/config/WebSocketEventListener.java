package com.comeon.websocket.global.config;

import com.comeon.websocket.user.application.MeetingMemberInfo;
import com.comeon.websocket.user.application.MeetingMemberInfoProvider;
import com.comeon.websocket.global.utils.StompSessionAttrUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private static final String PREFIX_SUB_MEETING = "/sub/meetings/";
    private final MeetingMemberInfoProvider meetingMemberInfoProvider;

    @EventListener
    public void handleMeetingSubscribeListener(SessionSubscribeEvent event) {
        log.info("start meeting subscribe event");
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headerAccessor.getDestination();

        Long meetingId = getMeetingIdBy(destination);
        if (meetingId != null) {
            Map<String, Object> attributes = headerAccessor.getSessionAttributes();
            String token = StompSessionAttrUtils.getToken(attributes);
            MeetingMemberInfo meetingMemberInfo = meetingMemberInfoProvider.getMeetingMemberInfoBy(token, meetingId);

            // TODO 레디스에서 모임 회원 리스트 조회
            // TODO 리스트에 회원 추가, 레디스 저장
            // TODO 리스트 변경사항 카프카에 적재

            if (meetingMemberInfo != null) {
                log.info("meeting member info: {}", meetingMemberInfo);
                // 세션에 모임 식별값 저장.
                StompSessionAttrUtils.setMeetingId(attributes, meetingId);
            } else {
                log.info("not meeting member...");
            }
        }
        log.info("end meeting subscribe event");
    }

    private Long getMeetingIdBy(String destination) {
        if (!destination.startsWith(PREFIX_SUB_MEETING)) {
            return null;
        }

        String afterSubMeetingPrefix = destination.substring(PREFIX_SUB_MEETING.length());
        if (afterSubMeetingPrefix.length() == 0) {
            return null;
        }

        int sliceIdx = afterSubMeetingPrefix.indexOf("/");
        if (sliceIdx == -1) {
            return null;
        }

        String meetingIdStr = afterSubMeetingPrefix.substring(0, sliceIdx);
        try {
            return Long.parseLong(meetingIdStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        log.info("start disconnect event");
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> attributes = headerAccessor.getSessionAttributes();
        Long meetingId = StompSessionAttrUtils.getMeetingId(attributes);
        Long userId = StompSessionAttrUtils.getUserId(attributes);
        // TODO 레디스에서 모임 회원 리스트 조회
        // TODO 회원 리스트에서 현재 유저 삭제
        // TODO 카프카에 리스트 변경사항 적재
        log.info("meetingId: {}, userId: {}", userId, meetingId);
        log.info("end disconnect event");
    }
}
