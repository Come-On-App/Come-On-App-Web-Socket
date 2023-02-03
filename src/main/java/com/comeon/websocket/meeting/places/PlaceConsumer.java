package com.comeon.websocket.meeting.places;

import com.comeon.websocket.global.kafka.MeetingResourceUpdatedMessage;
import com.comeon.websocket.global.message.ResourceUpdatedMessage;
import com.comeon.websocket.global.message.StompMessage;
import com.comeon.websocket.meeting.TargetResourceType;
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
public class PlaceConsumer {

    private final SimpMessagingTemplate simpMessagingTemplate;

    @KafkaListener(id = "place", topics = {"${kafka.topic.meeting-places}"})
    public void consume(@Payload MeetingResourceUpdatedMessage payload) {
        log.info("meeting-places updated. meetingId: {}", payload.getMeetingId());

        StompMessage<ResourceUpdatedMessage> message = StompMessage.resourceUpdated(
                ResourceUpdatedMessage.meetingResourceOf(
                        payload.getMeetingId(), TargetResourceType.PLACES
                )
        );
        simpMessagingTemplate.setHeaderInitializer(headerAccessor -> headerAccessor.setContentType(MediaType.APPLICATION_JSON));
        simpMessagingTemplate.convertAndSend("/sub/meetings/" + payload.getMeetingId(), message);

        log.info("Send place-update-message success. meetingId: {}", payload.getMeetingId());
    }
}
