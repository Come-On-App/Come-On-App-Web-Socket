package com.comeon.websocket.web.message.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MeetingSubUnsubKafkaMessage {

    private Long meetingId;
    private MessageType messageType;
    private Long targetUserId;
    private List<Long> subscribingUserIds = new ArrayList<>();

    public static MeetingSubUnsubKafkaMessage createSubMessage(Long meetingId, Long targetUserId, List<Long> subscribingUserIds) {
        return new MeetingSubUnsubKafkaMessage(meetingId, MessageType.SUB, targetUserId, subscribingUserIds);
    }

    public static MeetingSubUnsubKafkaMessage createUnsubMessage(Long meetingId, Long targetUserId, List<Long> subscribingUserIds) {
        return new MeetingSubUnsubKafkaMessage(meetingId, MessageType.UNSUB, targetUserId, subscribingUserIds);
    }

    public enum MessageType {
        SUB,
        UNSUB
        ;

        public boolean isSubscribeMessage() {
            return this == SUB;
        }
    }
}
