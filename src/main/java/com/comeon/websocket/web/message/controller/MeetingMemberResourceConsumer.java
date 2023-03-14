package com.comeon.websocket.web.message.controller;

import com.comeon.websocket.web.message.dto.MeetingMemberResourceMessage;
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
public class MeetingMemberResourceConsumer extends AbstractMessageConsumer {

    public MeetingMemberResourceConsumer(SimpMessagingTemplate messagingTemplate) {
        super(messagingTemplate);
    }

    @KafkaListener(id = "members", topics = {"${kafka.topic.meeting-members}"})
    public void consume(@Payload MemberListUpdateMessage message) {
        Long meetingId = message.getMeetingId();
        StompMessage<MeetingMemberResourceMessage> stompMessage =
                StompMessage.resourceUpdated(MeetingMemberResourceMessage.create(meetingId));

        messagingTemplate.setHeaderInitializer(
                headerAccessor -> headerAccessor.setContentType(MediaType.APPLICATION_JSON)
        );

        String destination = "/sub/meetings/" + meetingId;
        messagingTemplate.convertAndSend(destination, stompMessage);
        log.info("[meeting-members-update] send to meeting -> destination: {}", destination);
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberListUpdateMessage {
        private Long meetingId;
    }
}
