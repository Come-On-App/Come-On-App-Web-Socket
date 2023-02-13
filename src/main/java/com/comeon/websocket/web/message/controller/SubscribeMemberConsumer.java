package com.comeon.websocket.web.message.controller;

import com.comeon.websocket.web.message.dto.MeetingSubUnsubEventMessage;
import com.comeon.websocket.web.message.dto.MeetingSubUnsubKafkaMessage;
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
    public void consume(@Payload MeetingSubUnsubKafkaMessage payload) {
        simpMessagingTemplate.setHeaderInitializer(headerAccessor -> {
            headerAccessor.setContentType(MediaType.APPLICATION_JSON);
        });

        Long meetingId = payload.getMeetingId();
        StompMessage<MeetingSubscriberResponseData> SubscribingUserIdsMessage = StompMessage.meetingSubscribeUsers(
                new MeetingSubscriberResponseData(meetingId, payload.getSubscribingUserIds())
        );
        MeetingSubUnsubEventMessage subUnsubEventMessage = MeetingSubUnsubEventMessage.create(meetingId, payload.getTargetUserId());

        String destination = "/sub/meetings/" + meetingId;
        if (payload.getMessageType().isSubscribeMessage()) {
            simpMessagingTemplate.convertAndSend(destination, StompMessage.meetingSubscribeEvent(subUnsubEventMessage));
        } else {
            simpMessagingTemplate.convertAndSend(destination, StompMessage.meetingUnsubscribeEvent(subUnsubEventMessage));
        }
        simpMessagingTemplate.convertAndSend(destination, SubscribingUserIdsMessage);
    }
}
