package com.comeon.websocket.web.message.controller;

import com.comeon.websocket.web.message.dto.MeetingPlaceUnlockResourceMessage;
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
public class MeetingPlaceUnlockConsumer extends AbstractMessageConsumer {

    public MeetingPlaceUnlockConsumer(SimpMessagingTemplate messagingTemplate) {
        super(messagingTemplate);
    }

    @KafkaListener(id = "meeting-place-unlock", topics = {"${kafka.topic.meeting-place-unlock}"})
    public void consume(@Payload MeetingPlaceUnlockMessage payload) {
        Long meetingId = payload.getMeetingId();
        StompMessage<MeetingPlaceUnlockResourceMessage> stompMessage =
                StompMessage.resourceUpdated(MeetingPlaceUnlockResourceMessage.create(meetingId, payload.getMeetingPlaceId(), payload.getUserId()));

        messagingTemplate.setHeaderInitializer(
                headerAccessor -> headerAccessor.setContentType(MediaType.APPLICATION_JSON)
        );

        messagingTemplate.convertAndSend("/sub/meetings/" + meetingId, stompMessage);
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeetingPlaceUnlockMessage {
        private Long meetingId;
        private Long meetingPlaceId;
        private Long userId;
    }
}
