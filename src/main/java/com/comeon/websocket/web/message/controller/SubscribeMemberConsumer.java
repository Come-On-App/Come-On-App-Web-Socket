package com.comeon.websocket.web.message.controller;

import com.comeon.websocket.web.message.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscribeMemberConsumer {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final LockedMeetingPlaceProvider lockedMeetingPlaceProvider;

    @KafkaListener(id = "subscribe_members", topics = {"${kafka.topic.connecting-members}"})
    public void consume(@Payload MeetingSubUnsubKafkaMessage payload) {
        simpMessagingTemplate.setHeaderInitializer(headerAccessor -> headerAccessor.setContentType(MediaType.APPLICATION_JSON));

        Long meetingId = payload.getMeetingId();
        StompMessage<MeetingSubscriberResponseData> SubscribingUserIdsMessage = StompMessage.meetingSubscribeUsers(
                new MeetingSubscriberResponseData(meetingId, payload.getSubscribingUserIds())
        );
        MeetingSubUnsubEventMessage subUnsubEventMessage = MeetingSubUnsubEventMessage.create(meetingId, payload.getTargetUserId());

        String destination = "/sub/meetings/" + meetingId;
        if (payload.getMessageType().isSubscribeMessage()) {
            simpMessagingTemplate.convertAndSend(destination, StompMessage.meetingSubscribeEvent(subUnsubEventMessage));
            log.info("[subscribe] send to meeting -> destination: {}", destination);

            // 접속한 유저에게만 메시지 전송
            LockedMeetingPlaceListResponse lockedMeetingPlaceList = lockedMeetingPlaceProvider.getLockedMeetingPlaceList(meetingId);
            StompMessage<LockedMeetingPlaceListMessage> message = StompMessage.lockedMeetingPlaces(
                    LockedMeetingPlaceListMessage.create(
                            meetingId,
                            lockedMeetingPlaceList.getLockedPlaces().stream()
                                    .map(lockedPlaceSimple -> new LockedMeetingPlaceListMessage.LockedPlaceSimple(
                                            lockedPlaceSimple.getMeetingPlaceId(),
                                            lockedPlaceSimple.getLockingUserId()
                                    ))
                                    .collect(Collectors.toList())
                    )
            );

            String queueDestination = "/queue/meetings/" + meetingId;
            simpMessagingTemplate.convertAndSendToUser(String.valueOf(payload.getTargetUserId()), queueDestination, message);
            log.info("[locked-meeting-places] send to user -> user: {}, destination: {}", payload.getTargetUserId(), queueDestination);
        } else {
            simpMessagingTemplate.convertAndSend(destination, StompMessage.meetingUnsubscribeEvent(subUnsubEventMessage));
            log.info("[unsubscribe] send to meeting -> destination: {}", destination);
        }
        simpMessagingTemplate.convertAndSend(destination, SubscribingUserIdsMessage);
        log.info("[subscribing-users] send to meeting -> destination: {}", destination);
    }
}
