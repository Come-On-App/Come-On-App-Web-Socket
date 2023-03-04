package com.comeon.websocket.web.message.controller;

import com.comeon.websocket.web.message.dto.MeetingFixedDateResourceMessage;
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
public class MeetingFixedDateResourceConsumer extends AbstractMessageConsumer {

    public MeetingFixedDateResourceConsumer(SimpMessagingTemplate messagingTemplate) {
        super(messagingTemplate);
    }

    @KafkaListener(id = "fixed-date", topics = {"${kafka.topic.meeting-fixed-date}"})
    public void consume(@Payload FixedDateUpdateMessage message) {
        Long meetingId = message.getMeetingId();
        StompMessage<MeetingFixedDateResourceMessage> stompMessage =
                StompMessage.resourceUpdated(MeetingFixedDateResourceMessage.create(meetingId));

        messagingTemplate.setHeaderInitializer(
                headerAccessor -> headerAccessor.setContentType(MediaType.APPLICATION_JSON)
        );

        messagingTemplate.convertAndSend("/sub/meetings/" + meetingId, stompMessage);
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FixedDateUpdateMessage {
        private Long meetingId;
    }
}
