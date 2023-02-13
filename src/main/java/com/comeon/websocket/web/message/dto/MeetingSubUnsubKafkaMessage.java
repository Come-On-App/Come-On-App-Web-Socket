package com.comeon.websocket.web.message.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MeetingSubUnsubKafkaMessage {

    private Long meetingId;
    private MessageType messageType;
    private Long targetUserId;
    private Set<Long> subscribingUserIds = new HashSet<>();

    public static MeetingSubUnsubKafkaMessage createSubMessage(Long meetingId, Long targetUserId, Set<Long> subscribingUserIds) {
        return new MeetingSubUnsubKafkaMessage(meetingId, MessageType.SUB, targetUserId, subscribingUserIds);
    }

    public static MeetingSubUnsubKafkaMessage createUnsubMessage(Long meetingId, Long targetUserId, Set<Long> subscribingUserIds) {
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
