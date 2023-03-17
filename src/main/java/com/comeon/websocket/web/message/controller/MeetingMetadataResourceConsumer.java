package com.comeon.websocket.web.message.controller;

import com.comeon.websocket.web.message.dto.MeetingMetaResourceMessage;
import com.comeon.websocket.web.message.dto.MeetingTimeResourceMessage;
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
public class MeetingMetadataResourceConsumer extends AbstractMessageConsumer {

    public MeetingMetadataResourceConsumer(SimpMessagingTemplate messagingTemplate) {
        super(messagingTemplate);
    }

    @KafkaListener(id = "meeting-meta-data", topics = {"${kafka.topic.meeting-meta-data}"})
    public void consume(@Payload MeetingMetaDataUpdatedMessage message) {
        Long meetingId = message.getTargetMeetingId();
        StompMessage<MeetingMetaResourceMessage> stompMessage =
                StompMessage.resourceUpdated(MeetingMetaResourceMessage.create(meetingId));

        messagingTemplate.setHeaderInitializer(
                headerAccessor -> headerAccessor.setContentType(MediaType.APPLICATION_JSON)
        );

        String destination = "/sub/meetings/" + meetingId;
        messagingTemplate.convertAndSend(destination, stompMessage);
        log.info("[meeting-meta-data-update] send to meeting -> destination: {}", destination);
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeetingMetaDataUpdatedMessage {
        private Long targetMeetingId;
    }
}
