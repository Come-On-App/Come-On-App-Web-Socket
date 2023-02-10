package com.comeon.websocket.web.message.controller;

import com.comeon.websocket.web.config.MeetingSubscribeMembers;
import com.comeon.websocket.web.message.dto.MeetingSubscriberResponseData;
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
public class SubscribeMemberConsumer {

    private final SimpMessagingTemplate simpMessagingTemplate;

    @KafkaListener(id = "subscribe_members", topics = {"${kafka.topic.connecting-members}"})
    public void consume(@Payload MeetingSubscribeMembers payload) {
        Long meetingId = payload.getMeetingId();
        StompMessage<MeetingSubscriberResponseData> message = StompMessage.dataResponse(
                new MeetingSubscriberResponseData(meetingId, payload.getUserIds())
        );

        simpMessagingTemplate.setHeaderInitializer(headerAccessor -> {
            headerAccessor.setContentType(MediaType.APPLICATION_JSON);
        });

        simpMessagingTemplate.convertAndSend("/sub/meetings/" + meetingId, message);
    }
}
