package com.comeon.websocket.web.message.controller;

import com.comeon.websocket.web.message.dto.ResourceUpdatedMessage;
import com.comeon.websocket.web.message.dto.StompMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingPlaceConsumer {

    private final SimpMessagingTemplate simpMessagingTemplate;

    @KafkaListener(id = "place", topics = {"${kafka.topic.meeting-places}"})
    public void consume(@Payload MeetingResourceUpdatedMessage payload) {
        Long meetingId = payload.getMeetingId();
        StompMessage<ResourceUpdatedMessage> message = StompMessage.resourceUpdated(
                ResourceUpdatedMessage.ofMeetingPlaces(meetingId)
        );

        simpMessagingTemplate.setHeaderInitializer(headerAccessor -> {
            headerAccessor.setContentType(MediaType.APPLICATION_JSON);
        });

        simpMessagingTemplate.convertAndSend("/sub/meetings/" + meetingId, message);
    }
}
