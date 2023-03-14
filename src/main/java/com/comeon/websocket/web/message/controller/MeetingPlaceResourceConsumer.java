package com.comeon.websocket.web.message.controller;

import com.comeon.websocket.web.message.dto.MeetingPlaceResourceMessage;
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
public class MeetingPlaceResourceConsumer extends AbstractMessageConsumer {

    public MeetingPlaceResourceConsumer(SimpMessagingTemplate messagingTemplate) {
        super(messagingTemplate);
    }

    @KafkaListener(id = "places", topics = {"${kafka.topic.meeting-places}"})
    public void consume(@Payload PlaceListUpdateMessage payload) {
        Long meetingId = payload.getTargetMeetingId();
        StompMessage<MeetingPlaceResourceMessage> stompMessage =
                StompMessage.resourceUpdated(MeetingPlaceResourceMessage.create(meetingId));

        messagingTemplate.setHeaderInitializer(
                headerAccessor -> headerAccessor.setContentType(MediaType.APPLICATION_JSON)
        );

        String destination = "/sub/meetings/" + meetingId;
        messagingTemplate.convertAndSend(destination, stompMessage);
        log.info("[meeting-place-update] send to meeting -> destination: {}", destination);
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaceListUpdateMessage {
        private Long targetMeetingId;
    }
}
