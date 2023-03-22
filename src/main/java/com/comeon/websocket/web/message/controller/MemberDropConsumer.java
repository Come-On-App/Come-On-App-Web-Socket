package com.comeon.websocket.web.message.controller;

import com.comeon.websocket.web.message.dto.MeetingMemberResourceMessage;
import com.comeon.websocket.web.message.dto.MemberDroppedMessage;
import com.comeon.websocket.web.message.dto.StompMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MemberDropConsumer extends AbstractMessageConsumer {

    public MemberDropConsumer(SimpMessagingTemplate messagingTemplate) {
        super(messagingTemplate);
    }

    @KafkaListener(id = "member_drop", topics = {"${kafka.topic.member-drop}"})
    public void consume(@Payload MemberDropMessage message) {
        Long meetingId = message.getMeetingId();
        Long userId = message.getUserId();
        log.debug("user(id:{}) at meeting(id:{}) drop...", userId, meetingId);

        StompMessage<MemberDroppedMessage> droppedMessage =
                StompMessage.dropped(MemberDroppedMessage.create(meetingId, userId));

        messagingTemplate.setHeaderInitializer(
                headerAccessor -> headerAccessor.setContentType(MediaType.APPLICATION_JSON)
        );

        String queueDestination = "/queue/meetings/" + meetingId;
        messagingTemplate.convertAndSendToUser(String.valueOf(userId), queueDestination, droppedMessage);
        log.info("[drop-member] send to user -> user: {}, destination: {}", userId, queueDestination);
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberDropMessage {
        private Long meetingId;
        private Long userId;
    }
}
